package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.myprojects.lovable_clone.entity.Plan;
import com.myprojects.lovable_clone.entity.Subscription;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.mapper.SubscriptionMapper;
import com.myprojects.lovable_clone.repository.PlanRepository;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import com.myprojects.lovable_clone.repository.SubscriptionRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final AuthUtils authUtils;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private final Integer FREE_TIER_PROJECTS_ALLOWED = 1;

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
    public void activateSubscriptionForUser(String stripeSubscriptionId, Long userId, Long planId) {
        boolean exists = subscriptionRepository.existsByStripeSubscriptionId(stripeSubscriptionId);
        if (exists) return;

        User user = getUser(userId);
        Plan plan = getPlan(planId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .stripeSubscriptionId(stripeSubscriptionId)
                .status(SubscriptionStatus.INCOMPLETE)
                .build();

        subscriptionRepository.save(subscription);

    }

    @Override
    @Transactional
    public void updateSubscription(String stripeSubscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {
        Subscription subscription = getSubscription(stripeSubscriptionId);
        boolean hasSubscriptionUpdated = false;
        if(status != null && subscription.getStatus() != status){
            subscription.setStatus(status);
            hasSubscriptionUpdated=true;
        }
        if(periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())){
            subscription.setCurrentPeriodStart(periodStart);
            hasSubscriptionUpdated=true;
        }
        if(periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())){
            subscription.setCurrentPeriodEnd(periodEnd);
            hasSubscriptionUpdated=true;
        }
        if(cancelAtPeriodEnd != null && !cancelAtPeriodEnd.equals(subscription.getCancelAtPeriodEnd())){
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            hasSubscriptionUpdated=true;
        }
        if(planId != null&& !planId.equals(subscription.getPlan().getId())){
            Plan plan = getPlan(planId);
            subscription.setPlan(plan);
            hasSubscriptionUpdated=true;
        }

        if(hasSubscriptionUpdated){
            log.debug("Subscription has been updated for sub id: {}",stripeSubscriptionId);
            subscriptionRepository.save(subscription);
        }
    }

    @Override
    public void cancelSubscription(String stripeSubscriptionId) {
        Subscription subscription = getSubscription(stripeSubscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);


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
    public void markSubscriptionPastDue(String stripeSubscriptionId) {
        Subscription subscription = getSubscription(stripeSubscriptionId);
        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
            log.debug("Subscription with stripeSubscriptionId id: {} is already marked as past due", stripeSubscriptionId);
        }
        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);

        //NOTIFY USER VIA EMAIL
    }

    @Override
    public boolean canCreateProject() {
        SubscriptionResponse currentSubscription = getCurrentSubscription();
        Long userId = authUtils.getCurrentUserId();
        int projectOwnedByUser = projectMemberRepository.countProjectOwnedByUser(userId);
        if(currentSubscription.plan() == null){
            return projectOwnedByUser < FREE_TIER_PROJECTS_ALLOWED; // free tier users can only create 1 project
        }
        //if user is already having a subscription.
        Integer maxProjectsForCurrentPlan = currentSubscription.plan().maxProjects();

        return projectOwnedByUser < maxProjectsForCurrentPlan;
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
