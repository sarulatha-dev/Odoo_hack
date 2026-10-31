package com.brightloop.subscription.catalog.discount.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brightloop.subscription.catalog.discount.DiscountScope;
import com.brightloop.subscription.catalog.discount.DiscountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DiscountRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull DiscountType type,
        @NotNull @DecimalMin("0.00") BigDecimal value,
        @DecimalMin("0.00") BigDecimal minAmount,
        @Min(1) Integer minQty,
        LocalDate startDate,
        LocalDate endDate,
        @Min(1) Integer maxUses,
        @NotNull DiscountScope appliesTo,
        Long productId) {
}
