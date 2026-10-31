package com.brightloop.subscription.billing.dto;

public record InvoicePrintResponse(
        CompanyInfo company,
        InvoiceResponse invoice) {
}
