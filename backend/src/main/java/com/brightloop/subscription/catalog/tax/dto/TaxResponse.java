package com.brightloop.subscription.catalog.tax.dto;

import java.math.BigDecimal;

import com.brightloop.subscription.catalog.tax.Tax;

public record TaxResponse(
        Long id,
        String name,
        BigDecimal percent,
        String gstType,
        String hsnSacCode,
        boolean defaultTax) {

    public static TaxResponse from(Tax tax) {
        return new TaxResponse(
                tax.getId(),
                tax.getName(),
                tax.getPercent(),
                tax.getGstType().name(),
                tax.getHsnSacCode(),
                tax.isDefaultTax());
    }
}
