package com.brightloop.subscription.metrics.dto;

import java.math.BigDecimal;
import java.util.List;

public record PricingCardResponse(
        Long planId,
        String name,
        String tier,
        BigDecimal price,
        String billingPeriod,
        List<String> features,
        boolean highlighted,
        String nudgeMessage) {
}
