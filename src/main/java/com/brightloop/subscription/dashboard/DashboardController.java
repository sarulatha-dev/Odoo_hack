package com.brightloop.subscription.dashboard;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.dashboard.dto.DashboardSummary;
import com.brightloop.subscription.dashboard.dto.OverdueInvoiceResponse;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummary summary(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String displayCurrency) {
        return dashboardService.summary(from, to, customerId, planId, status, currency, displayCurrency);
    }

    @GetMapping("/overdue")
    public PageResponse<OverdueInvoiceResponse> overdue(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String displayCurrency,
            @PageableDefault(size = 20, sort = "dueDate") Pageable pageable) {
        return dashboardService.overdue(customerId, planId, currency, displayCurrency, pageable);
    }

    @PostMapping("/overdue/{invoiceId}/reminder")
    public void sendReminder(@PathVariable Long invoiceId) {
        dashboardService.sendReminder(invoiceId);
    }
}
