package com.brightloop.subscription.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(max = 120) String fullName,
        @Size(min = 9, max = 100) String password,
        @Size(max = 160) String companyName,
        @Size(max = 30) String phone,
        @Size(max = 60) String country,
        @Size(max = 20) String customerType,
        @Size(max = 255) String billingAddress,
        @Size(max = 80) String city,
        @Size(max = 60) String state,
        @Size(max = 20) String postalCode,
        @Size(max = 60) String billingCountry,
        @Size(max = 20) String gstin,
        @Size(max = 30) String taxRegistrationType,
        @Size(max = 2000) String notes,
        String status,
        @Pattern(regexp = "^[A-Za-z]{3}$") String preferredCurrency) {
}
