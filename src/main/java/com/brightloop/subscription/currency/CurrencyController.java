package com.brightloop.subscription.currency;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brightloop.subscription.currency.dto.CurrencyRateRequest;
import com.brightloop.subscription.currency.dto.CurrencyRateResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public List<CurrencyRateResponse> list() {
        return currencyService.list();
    }

    @PutMapping("/{currency}")
    @PreAuthorize("hasRole('ADMIN')")
    public CurrencyRateResponse update(@PathVariable String currency,
            @Valid @RequestBody CurrencyRateRequest request) {
        return currencyService.upsert(currency, request);
    }
}
