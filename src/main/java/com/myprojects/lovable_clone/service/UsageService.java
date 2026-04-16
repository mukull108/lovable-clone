package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.myprojects.lovable_clone.dto.subscription.UsageTodayResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ProblemDetail;

public interface UsageService {
    UsageTodayResponse getTodayUsageOfUser();

    PlanLimitResponse getCurrentSubscriptionLimitsOfUser();
}
