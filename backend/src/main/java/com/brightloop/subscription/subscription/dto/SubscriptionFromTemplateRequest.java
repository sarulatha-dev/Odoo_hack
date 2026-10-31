package com.brightloop.subscription.subscription.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SubscriptionFromTemplateRequest(
        @NotNull Long templateId,
        Long customerId,
        @NotNull LocalDate startDate,
        LocalDate expiryDate,
        @Min(0) int paymentTermsDays,
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter ISO code") String currency) {
}
