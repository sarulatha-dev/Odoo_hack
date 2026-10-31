package com.brightloop.subscription.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueInvoiceResponse(
        Long id,
        String number,
        Long customerId,
        String customerName,
        String planName,
        LocalDate dueDate,
        long daysOverdue,
        String currency,
        BigDecimal total,
        BigDecimal paid,
        BigDecimal outstanding,
        BigDecimal outstandingInBase,
        BigDecimal outstandingDisplay) {
}
