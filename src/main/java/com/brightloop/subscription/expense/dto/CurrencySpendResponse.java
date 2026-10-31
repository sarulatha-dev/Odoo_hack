package com.brightloop.subscription.expense.dto;

import java.math.BigDecimal;

public record CurrencySpendResponse(
        String currency,
        BigDecimal amount,
        BigDecimal amountInBase,
        BigDecimal amountDisplay) {
}
