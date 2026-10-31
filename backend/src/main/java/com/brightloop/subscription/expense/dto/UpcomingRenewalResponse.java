package com.brightloop.subscription.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingRenewalResponse(
        Long subscriptionId,
        String number,
        String planName,
        LocalDate nextBillingDate,
        String currency,
        BigDecimal amountInBase,
        BigDecimal amountDisplay) {
}
