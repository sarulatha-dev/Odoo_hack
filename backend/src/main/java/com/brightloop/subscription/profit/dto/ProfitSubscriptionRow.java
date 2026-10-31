package com.brightloop.subscription.profit.dto;

import java.math.BigDecimal;

public record ProfitSubscriptionRow(
        Long subscriptionId,
        String number,
        String customerName,
        String planName,
        String status,
        BigDecimal revenueInBase,
        BigDecimal costInBase,
        BigDecimal profitInBase,
        BigDecimal marginPercent) {
}
