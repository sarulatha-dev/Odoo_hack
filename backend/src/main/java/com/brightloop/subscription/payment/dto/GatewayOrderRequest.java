package com.brightloop.subscription.payment.dto;

import jakarta.validation.constraints.NotNull;

public record GatewayOrderRequest(@NotNull Long invoiceId) {
}
