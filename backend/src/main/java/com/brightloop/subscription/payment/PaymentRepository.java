package com.brightloop.subscription.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brightloop.subscription.billing.InvoiceStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceIdOrderByPaidOnDesc(Long invoiceId);

    List<Payment> findTop5ByInvoiceCustomerIdOrderByPaidOnDesc(Long customerId);

    @Query(value = """
            select p from Payment p
            join fetch p.invoice i
            join fetch i.customer c
            where (:from is null or p.paidOn >= :from)
              and (:to is null or p.paidOn <= :to)
              and (:customerId is null or c.id = :customerId)
              and (:currency is null or i.currency = :currency)
            """,
            countQuery = """
            select count(p) from Payment p
            where (:from is null or p.paidOn >= :from)
              and (:to is null or p.paidOn <= :to)
              and (:customerId is null or p.invoice.customer.id = :customerId)
              and (:currency is null or p.invoice.currency = :currency)
            """)
    Page<Payment> search(@Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("customerId") Long customerId,
            @Param("currency") String currency,
            Pageable pageable);

    boolean existsByGatewayRef(String gatewayRef);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.invoice.id = :invoiceId")
    BigDecimal sumByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("""
            select p.invoice.id, coalesce(sum(p.amount), 0)
            from Payment p
            where p.invoice.id in :invoiceIds
            group by p.invoice.id
            """)
    List<Object[]> sumByInvoiceIds(@Param("invoiceIds") Collection<Long> invoiceIds);

    @Query("""
            select p.invoice.id, coalesce(sum(p.amount), 0)
            from Payment p
            where (:status is null or p.invoice.status = :status)
              and (:customerId is null or p.invoice.customer.id = :customerId)
              and (:from is null or p.invoice.issueDate >= :from)
              and (:to is null or p.invoice.issueDate <= :to)
              and (:currency is null or p.invoice.currency = :currency)
            group by p.invoice.id
            """)
    List<Object[]> sumByInvoiceFilters(@Param("status") InvoiceStatus status,
            @Param("customerId") Long customerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("currency") String currency);

    @Query("""
            select coalesce(sum(p.amount * p.invoice.exchangeRate), 0) from Payment p
            where (:from is null or p.paidOn >= :from)
              and (:to is null or p.paidOn <= :to)
              and (:customerId is null or p.invoice.customer.id = :customerId)
              and (:planId is null or p.invoice.subscription.plan.id = :planId)
              and (:status is null or p.invoice.status = :status)
              and (:currency is null or p.invoice.currency = :currency)
            """)
    BigDecimal sumInBase(@Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("customerId") Long customerId,
            @Param("planId") Long planId,
            @Param("status") InvoiceStatus status,
            @Param("currency") String currency);

    @Query("""
            select p from Payment p
            join fetch p.invoice i
            join fetch i.customer
            where (:from is null or p.paidOn >= :from)
              and (:to is null or p.paidOn <= :to)
              and (:customerId is null or i.customer.id = :customerId)
              and (:currency is null or i.currency = :currency)
            order by p.paidOn, p.id
            """)
    List<Payment> findForExport(@Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("customerId") Long customerId,
            @Param("currency") String currency);
}
