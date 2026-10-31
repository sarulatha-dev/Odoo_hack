package com.brightloop.subscription.expense.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseSummaryResponse(
        String displayCurrency,
        BigDecimal displayRate,
        BigDecimal totalSpendInBase,
        BigDecimal totalSpend,
        BigDecimal currentMonthSpendInBase,
        BigDecimal currentMonthSpend,
        BigDecimal monthlyBudget,
        boolean budgetWarning,
        List<MonthlySpendResponse> monthlySpend,
        List<SubscriptionSpendResponse> bySubscription,
        List<UpcomingRenewalResponse> upcomingRenewals,
        List<CurrencySpendResponse> byCurrency) {
}
