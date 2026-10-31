package com.brightloop.subscription.quotation.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TemplateRequest(
        @NotBlank @Size(max = 140) String name,
        @Min(1) int validityDays,
        @NotNull Long planId,
        @NotEmpty List<@Valid TemplateLineRequest> lines) {
}
