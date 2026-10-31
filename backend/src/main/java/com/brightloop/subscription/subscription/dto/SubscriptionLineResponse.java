package com.brightloop.subscription.subscription.dto;

import java.math.BigDecimal;

import com.brightloop.subscription.subscription.SubscriptionLine;

public record SubscriptionLineResponse(
        Long id,
        Long productId,
        String productName,
        Long variantId,
        String variantLabel,
        int qty,
        BigDecimal unitPrice,
        Long taxId,
        BigDecimal taxPercent,
        BigDecimal amount,
        String hsnSacCode) {

    public static SubscriptionLineResponse from(SubscriptionLine line) {
        String variantLabel = line.getVariant() == null ? null
                : line.getVariant().getAttribute() + ": " + line.getVariant().getValue();
        return new SubscriptionLineResponse(
                line.getId(),
                line.getProduct().getId(),
                line.getProduct().getName(),
                line.getVariant() == null ? null : line.getVariant().getId(),
                variantLabel,
                line.getQty(),
                line.getUnitPrice(),
                line.getTax() == null ? null : line.getTax().getId(),
                line.getTax() == null ? null : line.getTax().getPercent(),
                line.getAmount(),
                line.getHsnSacCode());
    }
}
