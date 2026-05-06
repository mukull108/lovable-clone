package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.myprojects.lovable_clone.entity.Plan;
import com.myprojects.lovable_clone.entity.Subscription;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.mapper.SubscriptionMapper;
import com.myprojects.lovable_clone.repository.PlanRepository;
import com.myprojects.lovable_clone.repository.SubscriptionRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final AuthUtils authUtils;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtils.getCurrentUserId();

        Subscription subscription = subscriptionRepository.findByIdAndStatusIn(userId, Set.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PAST_DUE,
                SubscriptionStatus.TRIALING)
        ).orElse(
                new Subscription()
        );

        return subscriptionMapper.toSubscriptionResponse(subscription);
    }

    @Override
    public void activateSubscriptionForUser(String subscriptionId, Long userId, Long planId) {
        boolean exists = subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);
        if (exists) return;

        User user = getUser(userId);
        Plan plan = getPlan(planId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .stripeSubscriptionId(subscriptionId)
                .status(SubscriptionStatus.INCOMPLETE)
                .build();

        subscriptionRepository.save(subscription);

    }

    @Override
    public void updateSubscription(String subscriptionId, SubscriptionStatus status, Instant currentPeriodStart, Instant currentPeriodEnd, Boolean cancelAtPeriodEnd, Long planId) {

    }

    @Override
    public void cancelSubscription(String subscriptionId) {

    }

    @Override
    public void renewSubscriptionPeriod(String stripeSubscriptionId, Instant periodStart, Instant periodEnd) {
        Subscription subscription = getSubscription(stripeSubscriptionId);

        Instant newStartTime = periodStart != null ? periodStart : subscription.getCurrentPeriodEnd();
        subscription.setCurrentPeriodStart(periodStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE || subscription.getStatus() == SubscriptionStatus.INCOMPLETE) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }

        subscriptionRepository.save(subscription);

    }

    @Override
    public void markSubscriptionPastDue(String subscriptionId) {

    }

    /// //utility methods
    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(userId)));
    }

    private Plan getPlan(Long planId) {
        return planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan", String.valueOf(planId)));
    }

    private Subscription getSubscription(String subId) {
        return subscriptionRepository.findByStripeSubscriptionId(subId).orElseThrow(() -> new ResourceNotFoundException("Subscription", String.valueOf(subId)));
    }
}
