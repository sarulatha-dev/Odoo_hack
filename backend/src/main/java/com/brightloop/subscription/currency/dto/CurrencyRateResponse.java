package com.brightloop.subscription.currency.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.brightloop.subscription.currency.CurrencyRate;

public record CurrencyRateResponse(String currency, BigDecimal rateToInr, Instant updatedAt) {

    public static CurrencyRateResponse from(CurrencyRate rate) {
        return new CurrencyRateResponse(rate.getCurrency(), rate.getRateToInr(), rate.getUpdatedAt());
    }
}
