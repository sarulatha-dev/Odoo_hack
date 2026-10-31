package com.brightloop.subscription.catalog.plan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.brightloop.subscription.catalog.plan.Plan;

public record PlanResponse(
        Long id,
        String name,
        String tier,
        BigDecimal price,
        String billingPeriod,
        int minQty,
        LocalDate availableFrom,
        LocalDate availableTo,
        boolean autoClose,
        boolean closable,
        boolean pausable,
        boolean renewable,
        List<String> features) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getTier().name(),
                plan.getPrice(),
                plan.getBillingPeriod().name(),
                plan.getMinQty(),
                plan.getAvailableFrom(),
                plan.getAvailableTo(),
                plan.isAutoClose(),
                plan.isClosable(),
                plan.isPausable(),
                plan.isRenewable(),
                List.copyOf(plan.getFeatures()));
    }
}
