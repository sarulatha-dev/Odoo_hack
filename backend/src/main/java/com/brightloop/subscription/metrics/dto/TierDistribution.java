package com.brightloop.subscription.metrics.dto;

public record TierDistribution(
        long low,
        long medium,
        long high,
        long total) {
}
