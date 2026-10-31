package com.brightloop.subscription.catalog.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VariantRequest(
        @NotBlank @Size(max = 80) String attribute,
        @NotBlank @Size(max = 120) String value,
        @NotNull BigDecimal extraPrice) {
}
