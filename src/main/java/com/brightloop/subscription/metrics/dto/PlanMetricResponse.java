package com.brightloop.subscription.metrics.dto;

import java.math.BigDecimal;

public record PlanMetricResponse(
        Long planId,
        String name,
        String tier,
        BigDecimal price,
        String billingPeriod,
        long activeSubscribers,
        long totalSubscribers,
        long closedSubscribers,
        BigDecimal churnRate,
        BigDecimal revenueInBase,
        long views,
        long selections,
        BigDecimal selectionRate,
        boolean underperforming) {
}
