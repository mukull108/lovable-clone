package com.myprojects.lovable_clone.repository;

import com.myprojects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.myprojects.lovable_clone.entity.Subscription;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {

    Optional<Subscription> findByIdAndStatusIn(Long userId, Set<SubscriptionStatus> active);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    boolean existsByStripeSubscriptionId(String stripeSubscriptionId);
}
