package com.brightloop.subscription.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CurrencyPreferenceRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}", message = "Currency must be a 3-letter ISO code") String currency) {
}
