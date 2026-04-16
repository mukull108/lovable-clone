package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.myprojects.lovable_clone.dto.subscription.UsageTodayResponse;
import com.myprojects.lovable_clone.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usage")
public class UsageController {
    private final UsageService usageService;

    @GetMapping("/today")
    public ResponseEntity<UsageTodayResponse> getTodayUsage(){
        return ResponseEntity.ok(usageService.getTodayUsageOfUser());
    }

    @GetMapping("/limits")
    public ResponseEntity<PlanLimitResponse> getPlanLimits(){
        Long userId = 1L;
        return ResponseEntity.ok(usageService.getCurrentSubscriptionLimitsOfUser());
    }
}
