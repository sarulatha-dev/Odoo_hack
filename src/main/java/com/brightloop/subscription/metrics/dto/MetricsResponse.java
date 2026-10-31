package com.brightloop.subscription.metrics.dto;

import java.math.BigDecimal;
import java.util.List;

public record MetricsResponse(
        List<PlanMetricResponse> plans,
        TierDistribution tierDistribution,
        FunnelResponse funnel,
        List<PlanMetricResponse> topBySubscribers,
        List<PlanMetricResponse> topByRevenue,
        List<PlanMetricResponse> underperforming,
        long totalViews,
        long totalSelections,
        long highViews,
        long highSelections,
        BigDecimal highSelectionRate) {
}
