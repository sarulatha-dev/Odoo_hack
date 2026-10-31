package com.brightloop.subscription.subscription;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.brightloop.subscription.catalog.plan.BillingPeriod;

public final class BillingPeriods {

    private BillingPeriods() {
    }

    public static LocalDate add(LocalDate date, BillingPeriod period) {
        return switch (period) {
            case DAILY -> date.plusDays(1);
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }
}
