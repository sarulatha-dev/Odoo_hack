package com.brightloop.subscription.catalog.plan;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Plan> findFirstByTier(PlanTier tier);
}
