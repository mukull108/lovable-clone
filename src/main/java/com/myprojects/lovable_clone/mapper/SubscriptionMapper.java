package com.myprojects.lovable_clone.mapper;

import com.myprojects.lovable_clone.dto.subscription.PlanResponse;
import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.myprojects.lovable_clone.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);
    PlanResponse toSubscriptionResponse(PlanResponse plan);

}
