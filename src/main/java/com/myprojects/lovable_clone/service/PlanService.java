package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.subscription.PlanResponse;

import java.util.List;

public interface PlanService {
    List<PlanResponse> getAllActivePlans();

}
