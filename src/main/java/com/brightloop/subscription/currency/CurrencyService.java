package com.brightloop.subscription.currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.common.ApiException;
import com.brightloop.subscription.currency.dto.CurrencyRateRequest;
import com.brightloop.subscription.currency.dto.CurrencyRateResponse;

@Service
public class CurrencyService {

    public static final String BASE_CURRENCY = "INR";

    private final CurrencyRateRepository currencyRateRepository;

    public CurrencyService(CurrencyRateRepository currencyRateRepository) {
        this.currencyRateRepository = currencyRateRepository;
    }

    @Transactional(readOnly = true)
    public List<CurrencyRateResponse> list() {
        return currencyRateRepository.findAll().stream().map(CurrencyRateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal rateToInr(String currency) {
        String code = normalize(currency);
        if (BASE_CURRENCY.equals(code)) {
            return BigDecimal.ONE;
        }
        return currencyRateRepository.findById(code)
                .map(CurrencyRate::getRateToInr)
                .orElseThrow(() -> ApiException.badRequest("No exchange rate configured for " + code));
    }

    @Transactional
    public CurrencyRateResponse upsert(String currency, CurrencyRateRequest request) {
        String code = normalize(currency);
        if (BASE_CURRENCY.equals(code)) {
            throw ApiException.badRequest("The base currency rate cannot be changed");
        }
        CurrencyRate rate = currencyRateRepository.findById(code)
                .orElseGet(() -> CurrencyRate.builder().currency(code).build());
        rate.setRateToInr(request.rateToInr());
        rate.setUpdatedAt(Instant.now());
        return CurrencyRateResponse.from(currencyRateRepository.save(rate));
    }

    public static String normalize(String currency) {
        return currency == null ? BASE_CURRENCY : currency.trim().toUpperCase(Locale.ROOT);
    }
}
