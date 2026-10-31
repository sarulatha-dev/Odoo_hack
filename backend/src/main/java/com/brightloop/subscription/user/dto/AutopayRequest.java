package com.brightloop.subscription.user.dto;

import jakarta.validation.constraints.Pattern;

public record AutopayRequest(
        boolean enabled,
        @Pattern(regexp = "CARD|UPI|NETBANKING", message = "Method must be CARD, UPI or NETBANKING") String method) {
}
