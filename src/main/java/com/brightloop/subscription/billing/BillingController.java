package com.brightloop.subscription.billing;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.billing.dto.BillingRunResult;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/run")
    @PreAuthorize("hasRole('ADMIN')")
    public BillingRunResult runNow(@RequestParam(required = false) LocalDate date) {
        return billingService.run(date == null ? LocalDate.now() : date);
    }
}
