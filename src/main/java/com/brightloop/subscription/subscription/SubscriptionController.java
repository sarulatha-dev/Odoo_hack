package com.brightloop.subscription.subscription;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.common.PageResponse;
import com.brightloop.subscription.subscription.dto.SubscriptionFromTemplateRequest;
import com.brightloop.subscription.subscription.dto.SubscriptionRequest;
import com.brightloop.subscription.subscription.dto.SubscriptionResponse;
import com.brightloop.subscription.subscription.dto.SubscriptionSummary;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public PageResponse<SubscriptionSummary> list(
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) Long customerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return subscriptionService.list(status, customerId, pageable);
    }

    @GetMapping("/{id}")
    public SubscriptionResponse get(@PathVariable Long id) {
        return subscriptionService.get(id);
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.create(request));
    }

    @PostMapping("/from-template")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<SubscriptionResponse> createFromTemplate(
            @Valid @RequestBody SubscriptionFromTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.createFromTemplate(request));
    }

    @PostMapping("/{id}/actions/{action}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public SubscriptionResponse performAction(@PathVariable Long id, @PathVariable String action) {
        return subscriptionService.performAction(id, action);
    }
}
