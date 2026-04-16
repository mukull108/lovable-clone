package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.myprojects.lovable_clone.dto.subscription.UsageTodayResponse;
import com.myprojects.lovable_clone.service.UsageService;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {
    @Override
    public UsageTodayResponse getTodayUsageOfUser() {
        return null;
    }

    @Override
    public PlanLimitResponse getCurrentSubscriptionLimitsOfUser() {
        return null;
    }
}
