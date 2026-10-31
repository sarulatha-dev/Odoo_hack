package com.brightloop.subscription.quotation.dto;

import java.math.BigDecimal;

import com.brightloop.subscription.quotation.TemplateLine;

public record TemplateLineResponse(
        Long id,
        Long productId,
        String productName,
        Long variantId,
        String variantLabel,
        int qty,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {

    public static TemplateLineResponse from(TemplateLine line) {
        String variantLabel = line.getVariant() == null ? null
                : line.getVariant().getAttribute() + ": " + line.getVariant().getValue();
        return new TemplateLineResponse(
                line.getId(),
                line.getProduct().getId(),
                line.getProduct().getName(),
                line.getVariant() == null ? null : line.getVariant().getId(),
                variantLabel,
                line.getQty(),
                line.getUnitPrice(),
                line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQty())));
    }
}
