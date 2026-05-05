package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.subscription.*;
import com.myprojects.lovable_clone.service.PaymentProcessor;
import com.myprojects.lovable_clone.service.PlanService;
import com.myprojects.lovable_clone.service.SubscriptionService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    private final PlanService planService;
    private final SubscriptionService subscriptionService;
    private final PaymentProcessor paymentProcessor;

    @Value("${stripe.webhook.secret}")
    private String STRIPE_WEBHOOK_SECRET;

    @GetMapping("/api/plans")
    public ResponseEntity<List<PlanResponse>> getAllPlans(){
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/api/me/subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription(){
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription());
    }

    @PostMapping("/api/payments/checkout")
    public ResponseEntity<CheckoutResponse> createCheckoutResponse(
            @RequestBody CheckoutRequest request
    ){
        return ResponseEntity.ok(paymentProcessor.createCheckoutSessionUrl(request));

    }

    @PostMapping("/api/payments/portal")
    public ResponseEntity<PortalResponse> openCustomerPortal(){
        return ResponseEntity.ok(paymentProcessor.openCustomerPortal());
    }

    @PostMapping("/webhooks/payment")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader)
    {
        try {
            Event event = Webhook.constructEvent(payload,sigHeader,STRIPE_WEBHOOK_SECRET);

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;

            if(deserializer.getObject().isPresent()){
                stripeObject = deserializer.getObject().get();
            }else{
                //Fallback: Deserialize from raw JSON
                try{
                    stripeObject = deserializer.deserializeUnsafe();
                    if(stripeObject == null){
                        log.error("Failed to deserialize webhook object for event {}: ", event.getType());
                        return ResponseEntity.ok().build();
                    }
                }
                catch (EventDataObjectDeserializationException e)
                {
                    log.error("unsafe deserialization failed for event {}: {}", event.getType(), e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Deserialization failed");
                }
            }

            //now extract the metadata only if it's a checkout session.
            Map<String, String> metadata = new HashMap<>();
            if(stripeObject instanceof  Session session){
                metadata = session.getMetadata();
            }

            //Pass to your processor
            paymentProcessor.handleWebhookEvent(event.getType(), stripeObject, metadata);
            return ResponseEntity.ok().build();

            } catch (SignatureVerificationException e) {

            throw new RuntimeException(e);
        }


    }


}
