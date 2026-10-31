package com.brightloop.subscription.metrics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.metrics.dto.PricingResponse;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public PricingResponse pricing() {
        return pricingService.pricing();
    }

    @PostMapping("/{planId}/view")
    public void logView(@PathVariable Long planId) {
        pricingService.logView(planId);
    }

    @PostMapping("/{planId}/select")
    public void logSelection(@PathVariable Long planId) {
        pricingService.logSelection(planId);
    }
}
