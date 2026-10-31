package com.brightloop.subscription.subscription.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.brightloop.subscription.subscription.Subscription;

public record SubscriptionResponse(
        Long id,
        String number,
        Long customerId,
        String customerName,
        Long planId,
        String planName,
        LocalDate startDate,
        LocalDate expiryDate,
        int paymentTermsDays,
        String status,
        LocalDate nextBillingDate,
        String currency,
        BigDecimal subtotal,
        List<String> allowedActions,
        List<SubscriptionLineResponse> lines,
        Instant createdAt) {

    public static SubscriptionResponse from(Subscription subscription) {
        BigDecimal subtotal = subscription.getLines().stream()
                .map(line -> line.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getNumber(),
                subscription.getCustomer().getId(),
                subscription.getCustomer().getFullName(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getStartDate(),
                subscription.getExpiryDate(),
                subscription.getPaymentTermsDays(),
                subscription.getStatus().name(),
                subscription.getNextBillingDate(),
                subscription.getCurrency(),
                subtotal,
                allowedActions(subscription),
                subscription.getLines().stream().map(SubscriptionLineResponse::from).toList(),
                subscription.getCreatedAt());
    }

    private static List<String> allowedActions(Subscription subscription) {
        return switch (subscription.getStatus()) {
            case DRAFT -> List.of("quote");
            case QUOTATION -> List.of("confirm");
            case CONFIRMED -> {
                var actions = new java.util.ArrayList<>(List.of("activate"));
                if (subscription.getPlan().isClosable()) {
                    actions.add("close");
                }
                yield actions;
            }
            case ACTIVE -> {
                var actions = new java.util.ArrayList<String>();
                if (subscription.getPlan().isClosable()) {
                    actions.add("close");
                }
                if (subscription.getPlan().isPausable()) {
                    actions.add("pause");
                }
                if (subscription.getPlan().isRenewable()) {
                    actions.add("renew");
                }
                yield actions;
            }
            case PAUSED -> {
                var actions = new java.util.ArrayList<>(List.of("resume"));
                if (subscription.getPlan().isClosable()) {
                    actions.add("close");
                }
                yield actions;
            }
            case CLOSED -> List.of();
        };
    }
}
