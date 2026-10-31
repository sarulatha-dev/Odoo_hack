package com.brightloop.subscription.profit.dto;

import java.math.BigDecimal;

public record ProfitGroupRow(
        Long id,
        String name,
        BigDecimal revenueInBase,
        BigDecimal costInBase,
        BigDecimal profitInBase,
        BigDecimal marginPercent) {
}
