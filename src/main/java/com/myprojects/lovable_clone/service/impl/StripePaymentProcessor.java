package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.CheckoutRequest;
import com.myprojects.lovable_clone.dto.subscription.CheckoutResponse;
import com.myprojects.lovable_clone.dto.subscription.PortalResponse;
import com.myprojects.lovable_clone.entity.Plan;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.repository.PlanRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.PaymentProcessor;
import com.myprojects.lovable_clone.service.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {
    private final AuthUtils authUtils;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Value("${client.url}")
    private String FRONTEND_URL;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId()).orElseThrow(() -> new ResourceNotFoundException("Plan", String.valueOf(request.planId())));
        Long userId = authUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(userId)));

        var params = SessionCreateParams.builder() //I have taken this from the strip subscription documentation
                .addLineItem(SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build()).setMode(SessionCreateParams.Mode.SUBSCRIPTION).setSubscriptionData(new SessionCreateParams.SubscriptionData.Builder().setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder().setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE).build()).build()).setSuccessUrl(FRONTEND_URL + "/success.html?session_id={CHECKOUT_SESSION_ID}").setCancelUrl(FRONTEND_URL + "/cancel.html").putMetadata("userId", String.valueOf(userId)).putMetadata("planId", String.valueOf(plan.getId()));

        try {
            String stripCustomerId = user.getStripCustomerId();
            if (stripCustomerId == null || stripCustomerId.isEmpty()) {
                params.setCustomerEmail(user.getUsername());
            } else {
                params.setCustomer(stripCustomerId); //stripe customer id
            }
            Session session = Session.create(params.build()); // making api call to the Strip Payment gateway to create a checkout session
            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PortalResponse openCustomerPortal() {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        // Handle the webhook event based on its type and the associated Stripe object
        log.info("Handling webhook event {}", type);

        switch (type) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject, metadata);
            case "customer.subscription.updated" -> handleSubscriptionUpdated((Subscription) stripeObject);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted((Subscription) stripeObject);
            case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
            case "invoice.payment_failed" -> handlePaymentFailed((Invoice) stripeObject);
            default -> {
                log.info("Skipping unhandled event type: {}", type);
            }
        }

    }

    private void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata) {
        if (session == null) {
            log.error("Session object is null in checkout.session.completed event");
            return;
        }
        Long userId = Long.parseLong(metadata.get("userId"));
        Long planId = Long.parseLong(metadata.get("planId"));

        User user = getUser(userId);
        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();
        if (user.getStripCustomerId() == null || user.getStripCustomerId().isEmpty()) {
            user.setStripCustomerId(customerId);
            userRepository.save(user);
        }
        subscriptionService.activateSubscriptionForUser(subscriptionId, userId, planId);

    }

    private void handleSubscriptionUpdated(Subscription subscription) {
        if (subscription == null) {
            log.error("Subscription object is null in customer.subscription.updated event");
            return;
        }
        SubscriptionStatus status = mapStripeStatusToOurEnum(subscription.getStatus());
        if (status == null) {
            log.error("Unknow status: {} for subscriptionId: {}", subscription.getStatus(), subscription.getId());
            return;
        }
        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant currentPeriodStart = toInstant(item.getCurrentPeriodStart());
        Instant currentPeriodEnd = toInstant(item.getCurrentPeriodEnd());

        Long planId = resolvePlanId(item.getPrice());
        subscriptionService.updateSubscription(subscription.getId(), status, currentPeriodStart, currentPeriodEnd, subscription.getCancelAtPeriodEnd(), planId);
    }

    private void handleSubscriptionDeleted(Subscription subscription) {
        if (subscription == null) {
            log.error("Subscription object is null in customer.subscription.deleted event");
            return;
        }
        subscriptionService.cancelSubscription(subscription.getId());

    }

    private void handleInvoicePaid(Invoice invoice) {
        String subscriptionId = extractSubscriptionIdFromInvoice(invoice);
        if (subscriptionId == null) return;
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId); //SDK calling the stripe server
            SubscriptionItem item = subscription.getItems().getData().get(0);
            Instant currentPeriodStart = toInstant(item.getCurrentPeriodStart());
            Instant currentPeriodEnd = toInstant(item.getCurrentPeriodEnd());
            subscriptionService.renewSubscriptionPeriod(subscriptionId, currentPeriodStart, currentPeriodEnd);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePaymentFailed(Invoice invoice) {
        String subscriptionId = extractSubscriptionIdFromInvoice(invoice);
        if (subscriptionId == null) return;

        subscriptionService.markSubscriptionPastDue(subscriptionId);
    }

    /// //utility methods
    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(userId)));
    }

    private SubscriptionStatus mapStripeStatusToOurEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }

    private Instant toInstant(Long epoch) {
        return epoch != null ? Instant.ofEpochSecond(epoch) : null;
    }

    private Long resolvePlanId(Price price) {
        if (price == null || price.getId() == null) return null;
        return planRepository.findByStripePriceId(price.getId()).map(Plan::getId).orElseThrow(() -> new ResourceNotFoundException("Plan with Stripe Price ID", price.getId()));
    }

    private String extractSubscriptionIdFromInvoice(Invoice invoice) {
        var parent = invoice.getParent();
        if (parent == null) return null;

        var subscriptionDetails = parent.getSubscriptionDetails();
        if (subscriptionDetails == null) return null;
        return subscriptionDetails.getSubscription();
    }


}
