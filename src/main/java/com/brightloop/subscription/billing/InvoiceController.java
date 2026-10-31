package com.brightloop.subscription.billing;

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

import com.brightloop.subscription.billing.dto.InvoicePrintResponse;
import com.brightloop.subscription.billing.dto.InvoiceResponse;
import com.brightloop.subscription.billing.dto.InvoiceSummary;
import com.brightloop.subscription.common.PageResponse;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public PageResponse<InvoiceSummary> list(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String currency,
            @PageableDefault(size = 20, sort = "issueDate") Pageable pageable) {
        return invoiceService.list(status, customerId, from, to, currency, pageable);
    }

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return invoiceService.get(id);
    }

    @GetMapping("/{id}/print")
    public InvoicePrintResponse print(@PathVariable Long id) {
        return invoiceService.print(id);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public InvoiceResponse confirm(@PathVariable Long id) {
        return invoiceService.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public InvoiceResponse cancel(@PathVariable Long id) {
        return invoiceService.cancel(id);
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public void send(@PathVariable Long id) {
        invoiceService.send(id);
    }
}
