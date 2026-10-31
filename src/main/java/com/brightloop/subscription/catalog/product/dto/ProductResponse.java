package com.brightloop.subscription.catalog.product.dto;

import java.math.BigDecimal;
import java.util.List;

import com.brightloop.subscription.catalog.product.Product;

public record ProductResponse(
        Long id,
        String name,
        String type,
        BigDecimal salesPrice,
        BigDecimal costPrice,
        String notes,
        String hsnSacCode,
        List<VariantResponse> variants) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getType().name(),
                product.getSalesPrice(),
                product.getCostPrice(),
                product.getNotes(),
                product.getHsnSacCode(),
                product.getVariants().stream().map(VariantResponse::from).toList());
    }
}
