package com.brightloop.subscription.profit.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProfitResponse(
        ProfitSummary summary,
        List<ProfitSubscriptionRow> subscriptions,
        List<ProfitSubscriptionRow> topProfitable,
        List<ProfitSubscriptionRow> lossMaking,
        List<ProfitGroupRow> plans,
        List<ProfitGroupRow> products,
        List<ProfitGroupRow> monthly) {
}
