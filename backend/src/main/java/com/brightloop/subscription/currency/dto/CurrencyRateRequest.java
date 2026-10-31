package com.brightloop.subscription.currency.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CurrencyRateRequest(@NotNull @DecimalMin("0.000001") BigDecimal rateToInr) {
}
