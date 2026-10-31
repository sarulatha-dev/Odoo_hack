package com.brightloop.subscription.export;

import java.time.LocalDate;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.subscription.SubscriptionStatus;

@RestController
@RequestMapping("/api/exports")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class ExportController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/invoices")
    public ResponseEntity<byte[]> invoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String currency) {
        return download("invoices.xlsx",
                exportService.invoices(status, customerId, from, to, currency));
    }

    @GetMapping("/payments")
    public ResponseEntity<byte[]> payments(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String currency) {
        return download("payments.xlsx", exportService.payments(from, to, customerId, currency));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<byte[]> subscriptions(
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long planId) {
        return download("subscriptions.xlsx",
                exportService.subscriptions(status, customerId, planId));
    }

    @GetMapping("/profit")
    public ResponseEntity<byte[]> profit(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long planId) {
        return download("profit.xlsx", exportService.profit(from, to, planId));
    }

    private ResponseEntity<byte[]> download(String filename, byte[] body) {
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(body);
    }
}
