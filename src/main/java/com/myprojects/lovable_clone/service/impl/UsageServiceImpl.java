package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.myprojects.lovable_clone.dto.subscription.UsageTodayResponse;
import com.myprojects.lovable_clone.entity.Plan;
import com.myprojects.lovable_clone.entity.Subscription;
import com.myprojects.lovable_clone.repository.UsageLogRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {
    private final SubscriptionServiceImpl subscriptionService;
    private final UsageLogRepository usageLogRepository;
    private final AuthUtils authUtils;

    @Override
    public UsageTodayResponse getTodayUsageOfUser() {
        Long userId = authUtils.getCurrentUserId();
        Subscription subscription = subscriptionService.getCurrentSubscriptionEntity(userId);
        Plan plan = subscription.getPlan();

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfNextDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Long tokenUsedToday = usageLogRepository.sumTokenUsedForUserBetween(userId, startOfDay, startOfNextDay);

        int tokenLimit = Boolean.TRUE.equals(plan.getUnlimitedAi()) ? Integer.MAX_VALUE : plan.getMaxTokensPerDay();

        return new UsageTodayResponse(
                tokenUsedToday.intValue(),
                tokenLimit,
                0,
                plan.getMaxPreviews()
        );
    }

    @Override
    public PlanLimitResponse getCurrentSubscriptionLimitsOfUser() {
        Long userId = authUtils.getCurrentUserId();
        Subscription subscription = subscriptionService.getCurrentSubscriptionEntity(userId);
        Plan plan = subscription.getPlan();

        return new PlanLimitResponse(
                plan.getName(),
                plan.getMaxTokensPerDay(),
                plan.getMaxProjects(),
                plan.getUnlimitedAi()
        );
    }
}
