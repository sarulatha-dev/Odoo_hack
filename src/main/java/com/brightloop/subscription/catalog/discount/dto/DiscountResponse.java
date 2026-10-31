package com.brightloop.subscription.catalog.discount.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brightloop.subscription.catalog.discount.Discount;

public record DiscountResponse(
        Long id,
        String name,
        String type,
        BigDecimal value,
        BigDecimal minAmount,
        Integer minQty,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxUses,
        int usedCount,
        String appliesTo,
        Long productId,
        String productName) {

    public static DiscountResponse from(Discount discount) {
        return new DiscountResponse(
                discount.getId(),
                discount.getName(),
                discount.getType().name(),
                discount.getValue(),
                discount.getMinAmount(),
                discount.getMinQty(),
                discount.getStartDate(),
                discount.getEndDate(),
                discount.getMaxUses(),
                discount.getUsedCount(),
                discount.getAppliesTo().name(),
                discount.getProduct() == null ? null : discount.getProduct().getId(),
                discount.getProduct() == null ? null : discount.getProduct().getName());
    }
}
