package com.brightloop.subscription.payment;

import java.math.BigDecimal;

import com.brightloop.subscription.payment.dto.GatewayOrderResponse;

public interface PaymentGateway {

    GatewayOrderResponse createOrder(BigDecimal amount, String currency, String receipt);

    boolean verifySignature(String orderId, String paymentId, String signature);
}
