package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.subscription.PlanResponse;
import com.myprojects.lovable_clone.entity.Plan;
import com.myprojects.lovable_clone.repository.PlanRepository;
import com.myprojects.lovable_clone.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;

    @Override
    public List<PlanResponse> getAllActivePlans() {
        return planRepository.findByActiveTrueOrderByPriceAsc()
                .stream()
                .map(this::toPlanResponse)
                .toList();
    }

    private PlanResponse toPlanResponse(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getStripePriceId(),
                plan.getMaxProjects(),
                plan.getMaxTokensPerDay(),
                plan.getMaxPreviews(),
                plan.getUnlimitedAi(),
                plan.getPrice().toPlainString()
        );
    }
}
