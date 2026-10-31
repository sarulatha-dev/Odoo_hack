package com.brightloop.subscription.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brightloop.subscription.billing.Invoice;

public record InvoiceSummary(
        Long id,
        String number,
        Long customerId,
        String customerName,
        Long subscriptionId,
        String subscriptionNumber,
        String status,
        String currency,
        BigDecimal total,
        BigDecimal totalInBase,
        LocalDate issueDate,
        LocalDate dueDate) {

    public static InvoiceSummary from(Invoice invoice) {
        return new InvoiceSummary(
                invoice.getId(),
                invoice.getNumber(),
                invoice.getCustomer().getId(),
                invoice.getCustomer().getFullName(),
                invoice.getSubscription().getId(),
                invoice.getSubscription().getNumber(),
                invoice.getStatus().name(),
                invoice.getCurrency(),
                invoice.getTotal(),
                invoice.getTotalInBase(),
                invoice.getIssueDate(),
                invoice.getDueDate());
    }
}
