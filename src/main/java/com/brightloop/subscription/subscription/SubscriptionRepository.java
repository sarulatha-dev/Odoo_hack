package com.brightloop.subscription.subscription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Page<Subscription> findByCustomerId(Long customerId, Pageable pageable);

    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);

    Page<Subscription> findByCustomerIdAndStatus(Long customerId, SubscriptionStatus status, Pageable pageable);

    Optional<Subscription> findByIdAndCustomerId(Long id, Long customerId);

    List<Subscription> findByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus status, LocalDate date);

    long countByStatus(SubscriptionStatus status);

    long countByPlanIdAndStatus(Long planId, SubscriptionStatus status);

    long countByPlanId(Long planId);

    List<Subscription> findByPlanId(Long planId);

    @Query("""
            select count(s) from Subscription s
            where (:customerId is null or s.customer.id = :customerId)
              and (:planId is null or s.plan.id = :planId)
            """)
    long countFiltered(@Param("customerId") Long customerId, @Param("planId") Long planId);

    @Query("""
            select count(s) from Subscription s
            where s.status = com.brightloop.subscription.subscription.SubscriptionStatus.ACTIVE
              and (:customerId is null or s.customer.id = :customerId)
              and (:planId is null or s.plan.id = :planId)
            """)
    long countActive(@Param("customerId") Long customerId, @Param("planId") Long planId);

    @Query("""
            select distinct s from Subscription s
            join fetch s.lines
            join fetch s.plan
            where s.status = com.brightloop.subscription.subscription.SubscriptionStatus.ACTIVE
              and (:planId is null or s.plan.id = :planId)
            """)
    List<Subscription> findActiveForMrr(@Param("planId") Long planId);

    @Query("""
            select distinct s from Subscription s
            join fetch s.lines
            join fetch s.customer
            join fetch s.plan
            where (:status is null or s.status = :status)
              and (:customerId is null or s.customer.id = :customerId)
              and (:planId is null or s.plan.id = :planId)
            order by s.id
            """)
    List<Subscription> findForExport(@Param("status") SubscriptionStatus status,
            @Param("customerId") Long customerId,
            @Param("planId") Long planId);
}
