package com.brightloop.subscription.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceService;
import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.billing.dto.InvoiceResponse;
import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.notification.NotificationService;
import com.brightloop.subscription.payment.dto.GatewayOrderResponse;
import com.brightloop.subscription.payment.dto.GatewayVerifyRequest;
import com.brightloop.subscription.payment.dto.PaymentRequest;
import com.brightloop.subscription.payment.dto.PaymentResponse;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final PaymentGateway gateway;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepository, InvoiceService invoiceService,
            PaymentGateway gateway, NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
        this.gateway = gateway;
        this.notificationService = notificationService;
    }

    @Transactional
    public InvoiceResponse record(Long invoiceId, PaymentRequest request) {
        Invoice invoice = invoiceService.getAuthorizedInvoice(invoiceId);
        validatePayable(invoice);
        BigDecimal outstanding = outstanding(invoice);
        if (request.amount().compareTo(outstanding) > 0) {
            throw ApiException.badRequest("Payment exceeds the outstanding amount of " + outstanding);
        }

        paymentRepository.save(Payment.builder()
                .invoice(invoice)
                .method(request.method())
                .amount(request.amount())
                .paidOn(request.paidOn() == null ? LocalDate.now() : request.paidOn())
                .note(request.note())
                .build());
        refreshStatus(invoice);
        notificationService.notify(invoice.getCustomer(), "PAYMENT_RECEIVED",
                "Payment received for " + invoice.getNumber(),
                "We received " + invoice.getCurrency() + " " + request.amount() + " against invoice "
                        + invoice.getNumber() + ".",
                "INVOICE", invoice.getId());
        return invoiceService.get(invoiceId);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> list(Long invoiceId) {
        Invoice invoice = invoiceService.getAuthorizedInvoice(invoiceId);
        return paymentRepository.findByInvoiceIdOrderByPaidOnDesc(invoice.getId()).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listAll(LocalDate from, LocalDate to, Long customerId,
            String currency, Pageable pageable) {
        String normalized = currency == null || currency.isBlank() ? null : currency.trim().toUpperCase();
        Page<Payment> page = paymentRepository.search(from, to, customerId, normalized, pageable);
        return PageResponse.from(page, PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public GatewayOrderResponse createGatewayOrder(Long invoiceId) {
        Invoice invoice = invoiceService.getAuthorizedInvoice(invoiceId);
        validatePayable(invoice);
        BigDecimal outstanding = outstanding(invoice);
        return gateway.createOrder(outstanding, invoice.getCurrency(), invoice.getNumber());
    }

    @Transactional
    public InvoiceResponse verifyGatewayPayment(GatewayVerifyRequest request) {
        Invoice invoice = invoiceService.getAuthorizedInvoice(request.invoiceId());
        validatePayable(invoice);
        if (paymentRepository.existsByGatewayRef(request.paymentId())) {
            throw ApiException.badRequest("This gateway payment has already been recorded");
        }
        if (!gateway.verifySignature(request.orderId(), request.paymentId(), request.signature())) {
            throw ApiException.badRequest("Invalid gateway signature");
        }

        BigDecimal outstanding = outstanding(invoice);
        paymentRepository.save(Payment.builder()
                .invoice(invoice)
                .method(PaymentMethod.GATEWAY)
                .amount(outstanding)
                .paidOn(LocalDate.now())
                .note("Razorpay order " + request.orderId())
                .gatewayRef(request.paymentId())
                .build());
        refreshStatus(invoice);
        notificationService.notify(invoice.getCustomer(), "PAYMENT_RECEIVED",
                "Payment received for " + invoice.getNumber(),
                "Gateway payment of " + invoice.getCurrency() + " " + outstanding + " was confirmed for invoice "
                        + invoice.getNumber() + ".",
                "INVOICE", invoice.getId());
        return invoiceService.get(invoice.getId());
    }

    @Transactional
    public boolean chargeAutopay(Invoice invoice) {
        if (!invoice.getCustomer().isAutopayEnabled() || invoice.getCustomer().getAutopayToken() == null) {
            return false;
        }
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            return false;
        }
        BigDecimal outstanding = invoice.getTotal().subtract(invoiceService.paidAmount(invoice));
        if (outstanding.signum() <= 0) {
            return false;
        }
        paymentRepository.save(Payment.builder()
                .invoice(invoice)
                .method(PaymentMethod.GATEWAY)
                .amount(outstanding)
                .paidOn(LocalDate.now())
                .note("Autopay via " + invoice.getCustomer().getAutopayMethod() + " (simulated stored card)")
                .gatewayRef("autopay_" + invoice.getId() + "_" + System.nanoTime())
                .build());
        invoice.setStatus(InvoiceStatus.PAID);
        notificationService.notify(invoice.getCustomer(), "AUTOPAY_CHARGED",
                "Autopay charged for " + invoice.getNumber(),
                "Autopay via " + invoice.getCustomer().getAutopayMethod() + " charged "
                        + invoice.getCurrency() + " " + outstanding + " for invoice " + invoice.getNumber() + ".",
                "INVOICE", invoice.getId());
        return true;
    }

    private void validatePayable(Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw ApiException.badRequest("Cancelled invoices cannot be paid");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw ApiException.badRequest("Confirm the invoice before recording a payment");
        }
    }

    private BigDecimal outstanding(Invoice invoice) {
        BigDecimal outstanding = invoice.getTotal().subtract(invoiceService.paidAmount(invoice));
        if (outstanding.signum() <= 0) {
            throw ApiException.badRequest("Invoice is already paid");
        }
        return outstanding;
    }

    private void refreshStatus(Invoice invoice) {
        BigDecimal paid = invoiceService.paidAmount(invoice);
        if (paid.compareTo(invoice.getTotal()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (invoice.getStatus() == InvoiceStatus.PAID) {
            invoice.setStatus(InvoiceStatus.CONFIRMED);
        }
    }
}
