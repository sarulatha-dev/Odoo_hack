package com.brightloop.subscription.expense.dto;

import java.math.BigDecimal;

public record SubscriptionSpendResponse(
        Long subscriptionId,
        String number,
        String planName,
        String currency,
        BigDecimal amountInBase,
        BigDecimal amountDisplay) {
}
