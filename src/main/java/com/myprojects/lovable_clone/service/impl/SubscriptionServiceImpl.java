package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.config.StripeProperties;
import com.myprojects.lovable_clone.dto.subscription.CheckoutRequest;
import com.myprojects.lovable_clone.dto.subscription.CheckoutResponse;
import com.myprojects.lovable_clone.dto.subscription.PlanResponse;
import com.myprojects.lovable_clone.dto.subscription.PortalResponse;
import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.myprojects.lovable_clone.entity.Plan;
import com.myprojects.lovable_clone.entity.Subscription;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import com.myprojects.lovable_clone.exceptions.BadRequestException;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.repository.PlanRepository;
import com.myprojects.lovable_clone.repository.SubscriptionRepository;
import com.myprojects.lovable_clone.repository.UsageLogRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.SubscriptionService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.billingportal.Session;
import com.stripe.net.Webhook;
import com.stripe.param.billingportal.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private static final Set<SubscriptionStatus> CURRENT_STATUSES = Set.of(
            SubscriptionStatus.ACTIVE,
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.PAST_DUE,
            SubscriptionStatus.INCOMPLETE
    );

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final UsageLogRepository usageLogRepository;
    private final AuthUtils authUtils;
    private final StripeProperties stripeProperties;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtils.getCurrentUserId();
        Subscription subscription = getCurrentSubscriptionEntity(userId);

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfNextDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Long tokenUsedToday = usageLogRepository.sumTokenUsedForUserBetween(userId, startOfDay, startOfNextDay);

        return new SubscriptionResponse(
                toPlanResponse(subscription.getPlan()),
                subscription.getStatus().name(),
                subscription.getCurrentPeriodEnd(),
                tokenUsedToday
        );
    }

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Long userId = authUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.planId().toString()));

        if (plan.getStripePriceId() == null || plan.getStripePriceId().isBlank()) {
            throw new BadRequestException("Selected plan is not linked to a Stripe price");
        }

        ensureStripeConfigured();
        Stripe.apiKey = stripeProperties.getSecretKey();

        com.stripe.param.checkout.SessionCreateParams.Builder paramsBuilder =
                com.stripe.param.checkout.SessionCreateParams.builder()
                        .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                        .setSuccessUrl(stripeProperties.getCheckoutSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl(stripeProperties.getCheckoutCancelUrl())
                        .setClientReferenceId(userId.toString())
                        .addLineItem(
                                com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPrice(plan.getStripePriceId())
                                        .build()
                        )
                        .setSubscriptionData(
                                com.stripe.param.checkout.SessionCreateParams.SubscriptionData.builder()
                                        .putMetadata("userId", userId.toString())
                                        .putMetadata("planId", plan.getId().toString())
                                        .build()
                        );

        String existingCustomerId = subscriptionRepository.findTopByUserIdAndStripeCustomerIdIsNotNullOrderByCreatedAtDesc(userId)
                .map(Subscription::getStripeCustomerId)
                .filter(customerId -> !customerId.isBlank())
                .orElse(null);

        if (existingCustomerId != null) {
            paramsBuilder.setCustomer(existingCustomerId);
        } else {
            paramsBuilder.setCustomerEmail(user.getUsername());
        }

        try {
            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(paramsBuilder.build());
            return new CheckoutResponse(session.getUrl());
        } catch (StripeException ex) {
            throw new RuntimeException("Unable to create Stripe checkout session", ex);
        }
    }

    @Override
    public PortalResponse openCustomerPortal() {
        Long userId = authUtils.getCurrentUserId();
        Subscription subscription = getCurrentSubscriptionEntity(userId);
        if (subscription.getStripeCustomerId() == null || subscription.getStripeCustomerId().isBlank()) {
            throw new BadRequestException("Current subscription is not linked to a Stripe customer");
        }

        ensureStripeConfigured();
        Stripe.apiKey = stripeProperties.getSecretKey();

        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(subscription.getStripeCustomerId())
                .setReturnUrl(stripeProperties.getPortalReturnUrl())
                .build();

        try {
            Session session = Session.create(params);
            return new PortalResponse(session.getUrl());
        } catch (StripeException ex) {
            throw new RuntimeException("Unable to open Stripe customer portal", ex);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String stripeSignature) {
        ensureStripeWebhookConfigured();

        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            throw new BadRequestException("Invalid Stripe webhook signature");
        }

        switch (event.getType()) {
            case "customer.subscription.created", "customer.subscription.updated", "customer.subscription.deleted" ->
                    syncSubscriptionFromStripeEvent(event);
            case "checkout.session.completed" -> log.info("Stripe checkout completed: {}", event.getId());
            default -> log.info("Ignoring Stripe event type {}", event.getType());
        }
    }

    Subscription getCurrentSubscriptionEntity(Long userId) {
        return subscriptionRepository.findTopByUserIdAndStatusInOrderByCreatedAtDesc(userId, CURRENT_STATUSES)
                .or(() -> subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", userId.toString()));
    }

    PlanResponse toPlanResponse(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getStripePriceId(),
                plan.getMaxProjects(),
                plan.getMaxTokensPerDay(),
                plan.getMaxPreviews(),
                plan.getUnlimitedAi(),
                plan.getPrice().toPlainString()
        );
    }

    private void syncSubscriptionFromStripeEvent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = deserializer.getObject()
                .orElseThrow(() -> new RuntimeException("Unable to deserialize Stripe event " + event.getId()));

        if (!(stripeObject instanceof com.stripe.model.Subscription stripeSubscription)) {
            throw new RuntimeException("Unexpected Stripe object for event type " + event.getType());
        }

        com.stripe.model.SubscriptionItem subscriptionItem = stripeSubscription.getItems().getData().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Stripe subscription has no items"));

        String stripePriceId = subscriptionItem.getPrice() == null
                ? null
                : subscriptionItem.getPrice().getId();
        if (stripePriceId == null || stripePriceId.isBlank()) {
            throw new RuntimeException("Stripe subscription item has no price id");
        }

        Plan plan = planRepository.findByStripePriceId(stripePriceId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", stripePriceId));

        Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId())
                .orElseGet(Subscription::new);

        User user = resolveUserForStripeSubscription(stripeSubscription, subscription);

        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStripeCustomerId(stripeSubscription.getCustomer());
        subscription.setStripeSubscriptionId(stripeSubscription.getId());
        subscription.setCurrentPeriodStart(Instant.ofEpochSecond(subscriptionItem.getCurrentPeriodStart()));
        subscription.setCurrentPeriodEnd(Instant.ofEpochSecond(subscriptionItem.getCurrentPeriodEnd()));
        subscription.setCancelAtPeriodEnd(Boolean.TRUE.equals(stripeSubscription.getCancelAtPeriodEnd()));
        subscription.setStatus(mapStripeStatus(stripeSubscription.getStatus()));

        subscriptionRepository.save(subscription);
    }

    private User resolveUserForStripeSubscription(com.stripe.model.Subscription stripeSubscription,
                                                  Subscription existingSubscription) {
        if (existingSubscription.getUser() != null) {
            return existingSubscription.getUser();
        }

        String userIdValue = stripeSubscription.getMetadata().get("userId");
        if (userIdValue == null || userIdValue.isBlank()) {
            throw new RuntimeException("Stripe subscription metadata missing userId");
        }

        Long userId = Long.valueOf(userIdValue);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "incomplete", "incomplete_expired" -> SubscriptionStatus.INCOMPLETE;
            case "canceled", "unpaid", "paused" -> SubscriptionStatus.CANCELED;
            default -> throw new RuntimeException("Unsupported Stripe subscription status: " + stripeStatus);
        };
    }

    private void ensureStripeConfigured() {
        if (stripeProperties.getSecretKey() == null || stripeProperties.getSecretKey().isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured");
        }
    }

    private void ensureStripeWebhookConfigured() {
        ensureStripeConfigured();
        if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
    }
}
