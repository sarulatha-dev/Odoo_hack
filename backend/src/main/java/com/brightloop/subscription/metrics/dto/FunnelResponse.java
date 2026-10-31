package com.brightloop.subscription.metrics.dto;

public record FunnelResponse(
        long draft,
        long quotation,
        long confirmed,
        long active,
        long paused,
        long closed) {
}
