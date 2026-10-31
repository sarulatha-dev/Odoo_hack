package com.brightloop.subscription.subscription.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubscriptionLineRequest(
        @NotNull Long productId,
        Long variantId,
        @Min(1) int qty,
        @DecimalMin("0.00") BigDecimal unitPrice,
        Long taxId) {
}
