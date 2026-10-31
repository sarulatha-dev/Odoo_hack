package com.brightloop.subscription.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardSummary(
        String baseCurrency,
        String displayCurrency,
        BigDecimal displayRate,
        long activeSubscriptions,
        long totalSubscriptions,
        BigDecimal revenueInBase,
        BigDecimal revenue,
        BigDecimal paymentsInBase,
        BigDecimal payments,
        BigDecimal outstandingInBase,
        BigDecimal outstanding,
        long outstandingCount,
        BigDecimal overdueInBase,
        BigDecimal overdue,
        long overdueCount,
        LocalDate from,
        LocalDate to) {
}
