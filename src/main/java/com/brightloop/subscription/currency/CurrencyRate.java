package com.brightloop.subscription.currency;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "currency_rates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate {

    @Id
    @Column(length = 3)
    private String currency;

    @Column(name = "rate_to_inr", nullable = false, precision = 18, scale = 6)
    private BigDecimal rateToInr;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
