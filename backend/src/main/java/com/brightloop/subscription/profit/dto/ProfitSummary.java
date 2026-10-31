package com.brightloop.subscription.profit.dto;

import java.math.BigDecimal;

public record ProfitSummary(
        BigDecimal totalRevenueInBase,
        BigDecimal totalCostInBase,
        BigDecimal grossProfitInBase,
        BigDecimal marginPercent,
        BigDecimal mrrInBase,
        BigDecimal outstandingInBase,
        BigDecimal gstCollectedInBase) {
}
