package com.brightloop.subscription.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findBySubscriptionIdAndBillingPeriodStart(Long subscriptionId, LocalDate billingPeriodStart);

    boolean existsBySubscriptionIdAndBillingPeriodStart(Long subscriptionId, LocalDate billingPeriodStart);

    boolean existsByCustomerId(Long customerId);

    List<Invoice> findTop5ByCustomerIdOrderByIssueDateDesc(Long customerId);

    @Query("""
            select coalesce(sum(i.totalInBase), 0) from Invoice i
            where i.customer.id = :customerId
              and i.status <> com.brightloop.subscription.billing.InvoiceStatus.CANCELLED
            """)
    BigDecimal sumInvoicedInBaseByCustomer(@Param("customerId") Long customerId);

    @Query("""
            select coalesce(sum(i.totalInBase), 0) from Invoice i
            where i.customer.id = :customerId
              and i.status not in (com.brightloop.subscription.billing.InvoiceStatus.CANCELLED,
                                   com.brightloop.subscription.billing.InvoiceStatus.PAID)
            """)
    BigDecimal sumOutstandingInBaseByCustomer(@Param("customerId") Long customerId);

    @Query("""
            select coalesce(sum(i.totalInBase), 0) from Invoice i
            where i.customer.id = :customerId
              and i.status not in (com.brightloop.subscription.billing.InvoiceStatus.CANCELLED,
                                   com.brightloop.subscription.billing.InvoiceStatus.PAID)
              and i.dueDate < :today
            """)
    BigDecimal sumOverdueInBaseByCustomer(@Param("customerId") Long customerId, @Param("today") LocalDate today);

    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);

    List<Invoice> findByStatus(InvoiceStatus status);

    @Query("""
            select i from Invoice i
            where (:status is null or i.status = :status)
              and (:customerId is null or i.customer.id = :customerId)
              and (:from is null or i.issueDate >= :from)
              and (:to is null or i.issueDate <= :to)
              and (:currency is null or i.currency = :currency)
            """)
    Page<Invoice> search(@Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency,
            Pageable pageable);

    @Query("""
            select distinct i from Invoice i
            join fetch i.customer
            left join fetch i.lines
            where (:status is null or i.status = :status)
              and (:customerId is null or i.customer.id = :customerId)
              and (:from is null or i.issueDate >= :from)
              and (:to is null or i.issueDate <= :to)
              and (:currency is null or i.currency = :currency)
            order by i.id
            """)
    List<Invoice> findForExport(@Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency);

    @Query("""
            select coalesce(sum(i.subtotal * i.exchangeRate), 0) from Invoice i
            where i.status <> com.brightloop.subscription.billing.InvoiceStatus.CANCELLED
              and (:status is null or i.status = :status)
              and (:customerId is null or i.customer.id = :customerId)
              and (:planId is null or i.subscription.plan.id = :planId)
              and (:from is null or i.issueDate >= :from)
              and (:to is null or i.issueDate <= :to)
              and (:currency is null or i.currency = :currency)
            """)
    BigDecimal sumRevenueInBase(@Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("planId") Long planId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency);

    @Query("""
            select coalesce(sum(i.taxTotal * i.exchangeRate), 0) from Invoice i
            where i.status <> com.brightloop.subscription.billing.InvoiceStatus.CANCELLED
              and (:planId is null or i.subscription.plan.id = :planId)
              and (:from is null or i.issueDate >= :from)
              and (:to is null or i.issueDate <= :to)
            """)
    BigDecimal sumTaxInBase(@Param("planId") Long planId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            select distinct i from Invoice i
            join fetch i.lines l
            join fetch l.product
            join fetch i.subscription s
            join fetch s.plan
            join fetch s.customer
            where i.status = com.brightloop.subscription.billing.InvoiceStatus.PAID
              and (:from is null or i.issueDate >= :from)
              and (:to is null or i.issueDate <= :to)
              and (:planId is null or s.plan.id = :planId)
            """)
    List<Invoice> findPaidForProfit(@Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("planId") Long planId);

    @Query("""
            select i from Invoice i
            where i.status not in (com.brightloop.subscription.billing.InvoiceStatus.CANCELLED,
                                   com.brightloop.subscription.billing.InvoiceStatus.PAID)
              and (:status is null or i.status = :status)
              and (:customerId is null or i.customer.id = :customerId)
              and (:planId is null or i.subscription.plan.id = :planId)
              and (:from is null or i.issueDate >= :from)
              and (:to is null or i.issueDate <= :to)
              and (:currency is null or i.currency = :currency)
            """)
    List<Invoice> findOutstanding(@Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("planId") Long planId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency);

    @Query("""
            select i from Invoice i
            where i.status not in (com.brightloop.subscription.billing.InvoiceStatus.CANCELLED,
                                   com.brightloop.subscription.billing.InvoiceStatus.PAID)
              and i.dueDate < :today
              and (:customerId is null or i.customer.id = :customerId)
              and (:planId is null or i.subscription.plan.id = :planId)
              and (:currency is null or i.currency = :currency)
            """)
    Page<Invoice> findOverdue(@Param("today") LocalDate today,
            @Param("customerId") Long customerId,
            @Param("planId") Long planId,
            @Param("currency") String currency,
            Pageable pageable);
}
