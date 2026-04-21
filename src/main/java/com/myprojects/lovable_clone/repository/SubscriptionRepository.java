package com.myprojects.lovable_clone.repository;

import com.myprojects.lovable_clone.entity.Subscription;
import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Subscription> findTopByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, Collection<SubscriptionStatus> statuses);

    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<Subscription> findTopByUserIdAndStripeCustomerIdIsNotNullOrderByCreatedAtDesc(Long userId);
}
