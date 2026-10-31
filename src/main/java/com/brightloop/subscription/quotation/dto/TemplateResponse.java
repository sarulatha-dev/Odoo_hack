package com.brightloop.subscription.quotation.dto;

import java.util.List;

import com.brightloop.subscription.quotation.QuotationTemplate;

public record TemplateResponse(
        Long id,
        String name,
        int validityDays,
        Long planId,
        String planName,
        List<TemplateLineResponse> lines) {

    public static TemplateResponse from(QuotationTemplate template) {
        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getValidityDays(),
                template.getPlan().getId(),
                template.getPlan().getName(),
                template.getLines().stream().map(TemplateLineResponse::from).toList());
    }
}
