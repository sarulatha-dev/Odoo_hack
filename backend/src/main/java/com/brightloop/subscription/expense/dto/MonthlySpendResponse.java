package com.brightloop.subscription.expense.dto;

import java.math.BigDecimal;

public record MonthlySpendResponse(
        String month,
        BigDecimal amountInBase,
        BigDecimal amountDisplay) {
}
