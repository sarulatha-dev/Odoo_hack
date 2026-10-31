package com.brightloop.subscription.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GatewayVerifyRequest(
        @NotNull Long invoiceId,
        @NotBlank String orderId,
        @NotBlank String paymentId,
        @NotBlank String signature) {
}
