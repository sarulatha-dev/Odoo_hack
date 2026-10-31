package com.brightloop.subscription.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brightloop.subscription.subscription.Subscription;

public record SubscriptionSummary(
        Long id,
        String number,
        Long customerId,
        String customerName,
        Long planId,
        String planName,
        LocalDate startDate,
        LocalDate expiryDate,
        String status,
        LocalDate nextBillingDate,
        String currency,
        BigDecimal subtotal) {

    public static SubscriptionSummary from(Subscription subscription) {
        BigDecimal subtotal = subscription.getLines().stream()
                .map(line -> line.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SubscriptionSummary(
                subscription.getId(),
                subscription.getNumber(),
                subscription.getCustomer().getId(),
                subscription.getCustomer().getFullName(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getStartDate(),
                subscription.getExpiryDate(),
                subscription.getStatus().name(),
                subscription.getNextBillingDate(),
                subscription.getCurrency(),
                subtotal);
    }
}
