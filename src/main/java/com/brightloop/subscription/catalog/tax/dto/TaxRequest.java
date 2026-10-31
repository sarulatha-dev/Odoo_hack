package com.brightloop.subscription.catalog.tax.dto;

import java.math.BigDecimal;

import com.brightloop.subscription.catalog.tax.GstType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaxRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @DecimalMin("0.000") @DecimalMax("100.000") BigDecimal percent,
        @NotNull GstType gstType,
        @Size(max = 20) String hsnSacCode,
        boolean defaultTax) {
}
