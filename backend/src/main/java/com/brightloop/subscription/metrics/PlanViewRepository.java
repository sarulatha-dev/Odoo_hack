package com.brightloop.subscription.metrics;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanViewRepository extends JpaRepository<PlanView, Long> {

    long countByAction(PlanViewAction action);

    @Query("select pv.plan.id, count(pv) from PlanView pv where pv.action = :action group by pv.plan.id")
    List<Object[]> countByActionGrouped(@Param("action") PlanViewAction action);
}
