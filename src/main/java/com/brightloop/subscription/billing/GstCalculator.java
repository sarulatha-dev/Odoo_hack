package com.brightloop.subscription.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.brightloop.subscription.config.AppProperties;
import com.brightloop.subscription.user.IndianState;
import com.brightloop.subscription.user.User;

@Component
public class GstCalculator {

    public record GstBreakdown(
            BigDecimal cgst,
            BigDecimal sgst,
            BigDecimal igst,
            BigDecimal total,
            String placeOfSupply) {
    }

    private final AppProperties properties;

    public GstCalculator(AppProperties properties) {
        this.properties = properties;
    }

    public GstBreakdown calculate(BigDecimal taxable, BigDecimal percent, String currency, User customer) {
        BigDecimal rate = percent == null ? BigDecimal.ZERO : percent;
        String customerState = customer.getState() == null ? "North" : customer.getState().label();

        if (!"INR".equalsIgnoreCase(currency)) {
            return new GstBreakdown(zero(), zero(), zero(), zero(), "Export");
        }

        BigDecimal tax = taxable.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (isSameState(customer.getState())) {
            BigDecimal half = tax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal otherHalf = tax.subtract(half);
            return new GstBreakdown(half, otherHalf, zero(), tax, customerState);
        }
        return new GstBreakdown(zero(), zero(), tax, tax, customerState);
    }

    private boolean isSameState(IndianState customerState) {
        if (customerState == null) {
            return false;
        }
        return customerState.label().equalsIgnoreCase(properties.company().state().trim());
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
