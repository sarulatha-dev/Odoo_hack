package com.brightloop.subscription.quotation.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TemplateLineRequest(
        @NotNull Long productId,
        Long variantId,
        @Min(1) int qty,
        @DecimalMin("0.00") BigDecimal unitPrice) {
}
