package com.brightloop.subscription.catalog.product.dto;

import java.math.BigDecimal;
import java.util.List;

import com.brightloop.subscription.catalog.product.ProductType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 160) String name,
        @NotNull ProductType type,
        @NotNull @DecimalMin("0.00") BigDecimal salesPrice,
        @NotNull @DecimalMin("0.00") BigDecimal costPrice,
        String notes,
        @Size(max = 20) String hsnSacCode,
        List<@Valid VariantRequest> variants) {
}
