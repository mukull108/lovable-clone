package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.subscription.CheckoutRequest;
import com.myprojects.lovable_clone.dto.subscription.CheckoutResponse;
import com.myprojects.lovable_clone.dto.subscription.PortalResponse;
import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import com.stripe.model.checkout.Session;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Map;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    void activateSubscriptionForUser(String subscriptionId, Long userId, Long planId);

    void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant currentPeriodStart, Instant currentPeriodEnd, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String subscriptionId);

    void renewSubscriptionPeriod(String subscriptionId, Instant currentPeriodStart, Instant currentPeriodEnd);

    void markSubscriptionPastDue(String stripeSubscriptionId);
}
