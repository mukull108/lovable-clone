package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.CheckoutRequest;
import com.myprojects.lovable_clone.dto.subscription.CheckoutResponse;
import com.myprojects.lovable_clone.dto.subscription.PortalResponse;
import com.myprojects.lovable_clone.entity.Plan;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.repository.PlanRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.PaymentProcessor;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {
    private final AuthUtils authUtils;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Value("${client.url}")
    private String FRONTEND_URL;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId()).orElseThrow(
                () -> new ResourceNotFoundException("Plan", String.valueOf(request.planId())));
        Long userId = authUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(userId)));

        var params = SessionCreateParams.builder() //I have taken this from the strip subscription documentation
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build())
                                .build()
                )
                .setSuccessUrl(FRONTEND_URL + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(FRONTEND_URL + "/cancel.html")
                .putMetadata("userId", String.valueOf(userId))
                .putMetadata("planId", String.valueOf(plan.getId()));

        try {
            String stripCustomerId = user.getStripCustomerId();
            if(stripCustomerId == null || stripCustomerId.isEmpty()){
                params.setCustomerEmail(user.getUsername());
            }else{
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
            log.info("Received Stripe webhook event: type={}, object={}, metadata={}", type, stripeObject, metadata);

    }
}
