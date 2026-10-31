package com.brightloop.subscription.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceRepository;
import com.brightloop.subscription.billing.InvoiceService;
import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.common.MailService;
import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.config.AppProperties;
import com.brightloop.subscription.currency.CurrencyService;
import com.brightloop.subscription.dashboard.dto.DashboardSummary;
import com.brightloop.subscription.dashboard.dto.OverdueInvoiceResponse;
import com.brightloop.subscription.notification.NotificationService;
import com.brightloop.subscription.payment.PaymentRepository;
import com.brightloop.subscription.subscription.SubscriptionRepository;

@Service
public class DashboardService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceService invoiceService;
    private final CurrencyService currencyService;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final AppProperties properties;

    public DashboardService(InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            SubscriptionRepository subscriptionRepository,
            InvoiceService invoiceService,
            CurrencyService currencyService,
            MailService mailService,
            NotificationService notificationService,
            AppProperties properties) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceService = invoiceService;
        this.currencyService = currencyService;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary(LocalDate from, LocalDate to, Long customerId, Long planId,
            InvoiceStatus status, String currency, String displayCurrency) {
        String effectiveCurrency = currency == null ? null : CurrencyService.normalize(currency);
        String display = CurrencyService.normalize(displayCurrency == null ? "INR" : displayCurrency);
        BigDecimal displayRate = currencyService.rateToInr(display);

        BigDecimal revenueInBase = invoiceRepository
                .sumRevenueInBase(status, customerId, planId, from, to, effectiveCurrency)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal paymentsInBase = paymentRepository
                .sumInBase(from, to, customerId, planId, status, effectiveCurrency)
                .setScale(2, RoundingMode.HALF_UP);

        List<Invoice> outstandingInvoices = invoiceRepository
                .findOutstanding(status, customerId, planId, from, to, effectiveCurrency);
        Map<Long, BigDecimal> paidByInvoice = paidByInvoice(outstandingInvoices);
        LocalDate today = LocalDate.now();
        BigDecimal outstandingInBase = ZERO;
        BigDecimal overdueInBase = ZERO;
        long overdueCount = 0;
        for (Invoice invoice : outstandingInvoices) {
            BigDecimal outstanding = outstanding(invoice, paidByInvoice.getOrDefault(invoice.getId(), ZERO));
            BigDecimal outstandingBase = outstanding.multiply(invoice.getExchangeRate())
                    .setScale(2, RoundingMode.HALF_UP);
            outstandingInBase = outstandingInBase.add(outstandingBase);
            if (invoice.getDueDate().isBefore(today)) {
                overdueCount++;
                overdueInBase = overdueInBase.add(outstandingBase);
            }
        }

        long activeSubscriptions = subscriptionRepository.countActive(customerId, planId);
        long totalSubscriptions = subscriptionRepository.countFiltered(customerId, planId);

        return new DashboardSummary(
                "INR",
                display,
                displayRate,
                activeSubscriptions,
                totalSubscriptions,
                revenueInBase,
                toDisplay(revenueInBase, displayRate),
                paymentsInBase,
                toDisplay(paymentsInBase, displayRate),
                outstandingInBase,
                toDisplay(outstandingInBase, displayRate),
                outstandingInvoices.size(),
                overdueInBase,
                toDisplay(overdueInBase, displayRate),
                overdueCount,
                from,
                to);
    }

    @Transactional(readOnly = true)
    public PageResponse<OverdueInvoiceResponse> overdue(Long customerId, Long planId, String currency,
            String displayCurrency, Pageable pageable) {
        String effectiveCurrency = currency == null ? null : CurrencyService.normalize(currency);
        String display = CurrencyService.normalize(displayCurrency == null ? "INR" : displayCurrency);
        BigDecimal displayRate = currencyService.rateToInr(display);
        LocalDate today = LocalDate.now();

        Page<Invoice> page = invoiceRepository.findOverdue(today, customerId, planId, effectiveCurrency, pageable);
        return PageResponse.from(page, invoice -> {
            BigDecimal paid = paymentRepository.sumByInvoiceId(invoice.getId());
            BigDecimal outstanding = outstanding(invoice, paid);
            BigDecimal outstandingBase = outstanding.multiply(invoice.getExchangeRate())
                    .setScale(2, RoundingMode.HALF_UP);
            return new OverdueInvoiceResponse(
                    invoice.getId(),
                    invoice.getNumber(),
                    invoice.getCustomer().getId(),
                    invoice.getCustomer().getFullName(),
                    invoice.getSubscription().getPlan().getName(),
                    invoice.getDueDate(),
                    ChronoUnit.DAYS.between(invoice.getDueDate(), today),
                    invoice.getCurrency(),
                    invoice.getTotal(),
                    paid,
                    outstanding,
                    outstandingBase,
                    toDisplay(outstandingBase, displayRate));
        });
    }

    @Transactional
    public void sendReminder(Long invoiceId) {
        Invoice invoice = invoiceService.getAuthorizedInvoice(invoiceId);
        BigDecimal paid = paymentRepository.sumByInvoiceId(invoiceId);
        BigDecimal outstanding = outstanding(invoice, paid);
        long daysOverdue = Math.max(0, ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now()));
        mailService.send(invoice.getCustomer().getEmail(),
                "Reminder: invoice " + invoice.getNumber() + " is overdue",
                "Hello " + invoice.getCustomer().getFullName() + ",\n\n"
                        + "Invoice " + invoice.getNumber() + " from " + properties.company().name()
                        + " is " + daysOverdue + " day(s) overdue. "
                        + "Outstanding amount: " + invoice.getCurrency() + " " + outstanding + ".\n\n"
                        + "Please settle at your earliest convenience.\n\nThank you.");
        notificationService.notify(invoice.getCustomer(), "INVOICE_OVERDUE",
                "Invoice " + invoice.getNumber() + " is overdue",
                "Invoice " + invoice.getNumber() + " is " + daysOverdue + " day(s) overdue. Outstanding: "
                        + invoice.getCurrency() + " " + outstanding + ".",
                "INVOICE", invoice.getId());
    }

    private Map<Long, BigDecimal> paidByInvoice(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = invoices.stream().map(Invoice::getId).toList();
        Map<Long, BigDecimal> paid = new HashMap<>();
        for (Object[] row : paymentRepository.sumByInvoiceIds(ids)) {
            paid.put((Long) row[0], (BigDecimal) row[1]);
        }
        return paid;
    }

    private BigDecimal outstanding(Invoice invoice, BigDecimal paid) {
        return invoice.getTotal().subtract(paid).max(ZERO);
    }

    private BigDecimal toDisplay(BigDecimal amountInBase, BigDecimal rate) {
        return amountInBase.divide(rate, 2, RoundingMode.HALF_UP);
    }
}
