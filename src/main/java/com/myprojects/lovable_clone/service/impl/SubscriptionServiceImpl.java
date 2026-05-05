package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import com.myprojects.lovable_clone.service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    @Override
    public SubscriptionResponse getCurrentSubscription() {
        return null;
    }
    @Override
    public void activateSubscriptionForUser(String subscriptionId, Long userId, Long planId) {

    }

    @Override
    public void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant currentPeriodStart, Instant currentPeriodEnd, Boolean cancelAtPeriodEnd, Long planId) {

    }

    @Override
    public void cancelSubscription(String subscriptionId) {

    }

    @Override
    public void renewSubscriptionPeriod(String subscriptionId, Instant currentPeriodStart, Instant currentPeriodEnd) {

    }

    @Override
    public void markSubscriptionPastDue(String subscriptionId) {

    }
}
