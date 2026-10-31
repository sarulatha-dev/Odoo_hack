package com.brightloop.subscription.subscription.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubscriptionRequest(
        Long customerId,
        @NotNull Long planId,
        @NotNull LocalDate startDate,
        LocalDate expiryDate,
        @Min(0) int paymentTermsDays,
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter ISO code") String currency,
        @NotEmpty List<@Valid SubscriptionLineRequest> lines) {
}
