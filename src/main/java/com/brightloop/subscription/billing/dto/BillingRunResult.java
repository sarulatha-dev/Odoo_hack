package com.brightloop.subscription.billing.dto;

import java.time.Instant;

public record BillingRunResult(
        int subscriptionsProcessed,
        int invoicesCreated,
        int invoicesSkipped,
        Instant runAt) {
}
