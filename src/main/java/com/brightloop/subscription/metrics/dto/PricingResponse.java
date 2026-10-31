package com.brightloop.subscription.metrics.dto;

import java.util.List;

public record PricingResponse(
        String currency,
        List<PricingCardResponse> cards) {
}
