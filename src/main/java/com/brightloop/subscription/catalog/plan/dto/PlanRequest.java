package com.brightloop.subscription.catalog.plan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.brightloop.subscription.catalog.plan.BillingPeriod;
import com.brightloop.subscription.catalog.plan.PlanTier;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlanRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull PlanTier tier,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull BillingPeriod billingPeriod,
        @Min(1) int minQty,
        LocalDate availableFrom,
        LocalDate availableTo,
        boolean autoClose,
        boolean closable,
        boolean pausable,
        boolean renewable,
        List<@Size(max = 160) String> features) {
}
