package com.brightloop.subscription.billing.dto;

import java.math.BigDecimal;

import com.brightloop.subscription.billing.InvoiceLine;

public record InvoiceLineResponse(
        Long id,
        Long productId,
        String productName,
        String variantLabel,
        String description,
        String hsnSacCode,
        int qty,
        BigDecimal unitPrice,
        BigDecimal taxPercent,
        BigDecimal discountAmount,
        BigDecimal lineNet,
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        BigDecimal total) {

    public static InvoiceLineResponse from(InvoiceLine line) {
        String variantLabel = line.getVariant() == null ? null
                : line.getVariant().getAttribute() + ": " + line.getVariant().getValue();
        return new InvoiceLineResponse(
                line.getId(),
                line.getProduct().getId(),
                line.getProduct().getName(),
                variantLabel,
                line.getDescription(),
                line.getHsnSacCode(),
                line.getQty(),
                line.getUnitPrice(),
                line.getTaxPercent(),
                line.getDiscountAmount(),
                line.getLineNet(),
                line.getCgstAmount(),
                line.getSgstAmount(),
                line.getIgstAmount(),
                line.getTotal());
    }
}
