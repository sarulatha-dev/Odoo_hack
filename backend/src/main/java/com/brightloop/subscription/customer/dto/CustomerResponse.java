package com.brightloop.subscription.customer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CustomerResponse(
        Long id,
        String email,
        String fullName,
        String firstName,
        String lastName,
        String companyName,
        String phone,
        String country,
        String customerType,
        String billingAddress,
        String city,
        String state,
        String postalCode,
        String billingCountry,
        String gstin,
        String taxRegistrationType,
        String notes,
        boolean active,
        String status,
        String preferredCurrency,
        BigDecimal monthlyBudget,
        boolean autopayEnabled,
        String autopayMethod,
        Instant createdAt,
        SubscriptionSummary subscriptions,
        FinancialSummary financial,
        List<ActivityItem> activity) {

    public record SubscriptionSummary(long total, long active, long cancelled, List<String> plans) {
    }

    public record FinancialSummary(BigDecimal totalInvoiced, BigDecimal totalPaid,
            BigDecimal outstanding, BigDecimal overdue, String currency) {
    }

    public record ActivityItem(String type, String description, LocalDate date) {
    }
}
