package com.myprojects.lovable_clone.dto.subscription;

public record PlanResponse(
        Long id,
        String name,
        String stripePriceId,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Integer maxPreviews, //maximum number of previews allowed
        Boolean unlimitedAi, //unlimited access for LLM, ignore maxTokensPerDay if true.
        String price
) {
}
