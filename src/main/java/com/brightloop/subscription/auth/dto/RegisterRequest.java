package com.brightloop.subscription.auth.dto;

import com.brightloop.subscription.auth.PasswordPolicy;
import com.brightloop.subscription.user.IndianState;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE) String password,
        @NotBlank @Size(max = 120) String fullName,
        IndianState state,
        @Size(max = 20) String gstin,
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter ISO code") String preferredCurrency) {
}
