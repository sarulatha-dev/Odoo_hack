package com.brightloop.subscription.profit;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.profit.dto.ProfitResponse;

@RestController
@RequestMapping("/api/profit")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class ProfitController {

    private final ProfitService profitService;

    public ProfitController(ProfitService profitService) {
        this.profitService = profitService;
    }

    @GetMapping
    public ProfitResponse report(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Long planId) {
        return profitService.report(from, to, planId);
    }
}
