package com.brightloop.subscription.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brightloop.subscription.payment.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull PaymentMethod method,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        LocalDate paidOn,
        String note) {
}
