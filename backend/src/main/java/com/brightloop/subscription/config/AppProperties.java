package com.brightloop.subscription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Company company,
        Mail mail,
        Gateway gateway,
        Billing billing,
        Metrics metrics,
        String frontendUrl) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Company(String name, String state, String gstin, String currency) {
    }

    public record Mail(String from, boolean enabled) {
    }

    public record Gateway(Razorpay razorpay) {
        public record Razorpay(String keyId, String keySecret) {
        }
    }

    public record Billing(String cron) {
    }

    public record Metrics(java.math.BigDecimal underperformingChurnThreshold) {
    }
}
