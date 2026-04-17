package com.myprojects.lovable_clone.repository;

import com.myprojects.lovable_clone.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByActiveTrueOrderByPriceAsc();

    Optional<Plan> findByStripePriceId(String stripePriceId);
}
