package com.brightloop.subscription.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.brightloop.subscription.payment.Payment;

public record PaymentResponse(
        Long id,
        Long invoiceId,
        String invoiceNumber,
        String method,
        BigDecimal amount,
        LocalDate paidOn,
        String note,
        String gatewayRef) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getInvoice().getNumber(),
                payment.getMethod().name(),
                payment.getAmount(),
                payment.getPaidOn(),
                payment.getNote(),
                payment.getGatewayRef());
    }
}
