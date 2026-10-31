package com.brightloop.subscription.catalog.product.dto;

import java.math.BigDecimal;

import com.brightloop.subscription.catalog.product.ProductVariant;

public record VariantResponse(Long id, String attribute, String value, BigDecimal extraPrice) {

    public static VariantResponse from(ProductVariant variant) {
        return new VariantResponse(variant.getId(), variant.getAttribute(), variant.getValue(),
                variant.getExtraPrice());
    }
}
