package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.subscription.CheckoutRequest;
import com.myprojects.lovable_clone.dto.subscription.CheckoutResponse;
import com.myprojects.lovable_clone.dto.subscription.PortalResponse;
import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

    PortalResponse openCustomerPortal();

    void handleWebhook(String payload, String stripeSignature);
}
