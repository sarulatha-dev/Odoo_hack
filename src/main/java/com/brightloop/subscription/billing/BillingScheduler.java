package com.brightloop.subscription.billing;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.brightloop.subscription.billing.dto.BillingRunResult;

@Component
public class BillingScheduler {

    private static final Logger log = LoggerFactory.getLogger(BillingScheduler.class);

    private final BillingService billingService;

    public BillingScheduler(BillingService billingService) {
        this.billingService = billingService;
    }

    @Scheduled(cron = "${app.billing.cron}")
    public void runScheduledBilling() {
        BillingRunResult result = billingService.run(LocalDate.now());
        log.info("Scheduled billing finished: processed={} created={} skipped={}",
                result.subscriptionsProcessed(), result.invoicesCreated(), result.invoicesSkipped());
    }
}
