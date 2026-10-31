package com.brightloop.subscription.metrics;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.metrics.dto.MetricsResponse;

@RestController
@RequestMapping("/api/metrics")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/plans")
    public MetricsResponse plans() {
        return metricsService.plans();
    }
}
