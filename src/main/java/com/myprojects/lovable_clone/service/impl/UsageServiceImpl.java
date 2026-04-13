package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.myprojects.lovable_clone.dto.subscription.UsageTodayResponse;
import com.myprojects.lovable_clone.service.UsageService;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {
    @Override
    public UsageTodayResponse getTodayUsageOfUser(Long userId) {
        return null;
    }

    @Override
    public PlanLimitResponse getCurrentSubscriptionLimitsOfUser(Long userId) {
        return null;
    }
}
