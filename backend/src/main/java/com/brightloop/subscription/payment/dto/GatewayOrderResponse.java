package com.brightloop.subscription.payment.dto;

import java.math.BigDecimal;

public record GatewayOrderResponse(
        String orderId,
        BigDecimal amount,
        String currency,
        String keyId,
        boolean simulated) {
}
