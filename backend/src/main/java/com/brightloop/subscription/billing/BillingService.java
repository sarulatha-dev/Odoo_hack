package com.brightloop.subscription.billing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.dto.BillingRunResult;
import com.brightloop.subscription.payment.PaymentService;
import com.brightloop.subscription.subscription.BillingPeriods;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionService;
import com.brightloop.subscription.subscription.SubscriptionStatus;

@Service
public class BillingService {

    private static final int MAX_PERIODS_PER_RUN = 1000;

    private final SubscriptionService subscriptionService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public BillingService(SubscriptionService subscriptionService,
            InvoiceRepository invoiceRepository,
            InvoiceService invoiceService,
            PaymentService paymentService) {
        this.subscriptionService = subscriptionService;
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    @Transactional
    public BillingRunResult run(LocalDate today) {
        List<Subscription> due = subscriptionService.dueForBilling(today);
        int processed = 0;
        int created = 0;
        int skipped = 0;

        for (Subscription subscription : due) {
            processed++;
            LocalDate cursor = subscription.getNextBillingDate();
            int guard = 0;
            while (cursor != null && !cursor.isAfter(today) && guard < MAX_PERIODS_PER_RUN) {
                if (invoiceRepository.existsBySubscriptionIdAndBillingPeriodStart(subscription.getId(), cursor)) {
                    skipped++;
                } else {
                    Invoice invoice = invoiceService.generateForSubscription(subscription, cursor, true);
                    paymentService.chargeAutopay(invoice);
                    created++;
                }

                LocalDate next = BillingPeriods.add(cursor, subscription.getPlan().getBillingPeriod());
                if (subscription.getExpiryDate() != null && next.isAfter(subscription.getExpiryDate())) {
                    if (subscription.getPlan().isAutoClose()) {
                        subscription.setStatus(SubscriptionStatus.CLOSED);
                    }
                    subscription.setNextBillingDate(null);
                    cursor = null;
                } else {
                    subscription.setNextBillingDate(next);
                    cursor = next;
                }
                guard++;
            }
            subscriptionService.save(subscription);
        }

        return new BillingRunResult(processed, created, skipped, Instant.now());
    }
}
