package com.brightloop.subscription.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.brightloop.subscription.config.AppProperties;
import com.brightloop.subscription.payment.dto.GatewayOrderResponse;

@Service
public class RazorpayPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentGateway.class);
    private static final String BASE_URL = "https://api.razorpay.com/v1";

    private final AppProperties properties;
    private final RestClient restClient;

    public RazorpayPaymentGateway(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    @Override
    public GatewayOrderResponse createOrder(BigDecimal amount, String currency, String receipt) {
        if (!isConfigured()) {
            String orderId = "order_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("[razorpay:simulated] order={} amount={} {}", orderId, amount, currency);
            return new GatewayOrderResponse(orderId, amount, currency, "", true);
        }

        long smallestUnit = amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("/orders")
                .header("Authorization", basicAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "amount", smallestUnit,
                        "currency", currency,
                        "receipt", receipt))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new IllegalStateException("Razorpay did not return an order id");
        }
        return new GatewayOrderResponse(String.valueOf(response.get("id")), amount, currency,
                properties.gateway().razorpay().keyId(), false);
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (!isConfigured()) {
            log.info("[razorpay:simulated] accepting signature for order={} payment={}", orderId, paymentId);
            return true;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    properties.gateway().razorpay().keySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return MessageDigest.isEqual(hex.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.warn("Razorpay signature verification failed: {}", ex.getMessage());
            return false;
        }
    }

    private boolean isConfigured() {
        return properties.gateway().razorpay().keyId() != null
                && !properties.gateway().razorpay().keyId().isBlank()
                && properties.gateway().razorpay().keySecret() != null
                && !properties.gateway().razorpay().keySecret().isBlank();
    }

    private String basicAuth() {
        String raw = properties.gateway().razorpay().keyId() + ":"
                + properties.gateway().razorpay().keySecret();
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
