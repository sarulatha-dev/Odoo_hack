package com.brightloop.subscription.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.brightloop.subscription.billing.Invoice;

public record InvoiceResponse(
        Long id,
        String number,
        Long subscriptionId,
        String subscriptionNumber,
        Long customerId,
        String customerName,
        String status,
        String currency,
        BigDecimal exchangeRate,
        BigDecimal subtotal,
        BigDecimal taxTotal,
        BigDecimal discountTotal,
        BigDecimal total,
        BigDecimal totalInBase,
        BigDecimal paidAmount,
        BigDecimal outstanding,
        boolean overdue,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        String placeOfSupply,
        String companyGstin,
        String customerGstin,
        int retryCount,
        List<InvoiceLineResponse> lines,
        Instant createdAt) {

    public static InvoiceResponse from(Invoice invoice, BigDecimal paidAmount, boolean overdue) {
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        BigDecimal outstanding = invoice.getTotal().subtract(paid).max(BigDecimal.ZERO);
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getNumber(),
                invoice.getSubscription().getId(),
                invoice.getSubscription().getNumber(),
                invoice.getCustomer().getId(),
                invoice.getCustomer().getFullName(),
                invoice.getStatus().name(),
                invoice.getCurrency(),
                invoice.getExchangeRate(),
                invoice.getSubtotal(),
                invoice.getTaxTotal(),
                invoice.getDiscountTotal(),
                invoice.getTotal(),
                invoice.getTotalInBase(),
                paid,
                outstanding,
                overdue,
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getBillingPeriodStart(),
                invoice.getBillingPeriodEnd(),
                invoice.getPlaceOfSupply(),
                invoice.getCompanyGstin(),
                invoice.getCustomerGstin(),
                invoice.getRetryCount(),
                invoice.getLines().stream().map(InvoiceLineResponse::from).toList(),
                invoice.getCreatedAt());
    }
}
