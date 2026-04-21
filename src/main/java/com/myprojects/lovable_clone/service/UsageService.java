package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.myprojects.lovable_clone.dto.subscription.UsageTodayResponse;

public interface UsageService {
    UsageTodayResponse getTodayUsageOfUser();

    PlanLimitResponse getCurrentSubscriptionLimitsOfUser();
}
