package com.brightloop.subscription.payment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.billing.dto.InvoiceResponse;
import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.payment.dto.GatewayOrderRequest;
import com.brightloop.subscription.payment.dto.GatewayOrderResponse;
import com.brightloop.subscription.payment.dto.GatewayVerifyRequest;
import com.brightloop.subscription.payment.dto.PaymentRequest;
import com.brightloop.subscription.payment.dto.PaymentResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public PageResponse<PaymentResponse> listAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String currency,
            Pageable pageable) {
        return paymentService.listAll(from, to, customerId, currency, pageable);
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    public List<PaymentResponse> list(@PathVariable Long invoiceId) {
        return paymentService.list(invoiceId);
    }

    @PostMapping("/invoices/{invoiceId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public InvoiceResponse record(@PathVariable Long invoiceId, @Valid @RequestBody PaymentRequest request) {
        return paymentService.record(invoiceId, request);
    }

    @PostMapping("/payments/gateway/order")
    public GatewayOrderResponse createOrder(@Valid @RequestBody GatewayOrderRequest request) {
        return paymentService.createGatewayOrder(request.invoiceId());
    }

    @PostMapping("/payments/gateway/verify")
    public InvoiceResponse verify(@Valid @RequestBody GatewayVerifyRequest request) {
        return paymentService.verifyGatewayPayment(request);
    }
}
