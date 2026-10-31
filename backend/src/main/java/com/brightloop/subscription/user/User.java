package com.brightloop.subscription.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(name = "company_name", length = 160)
    private String companyName;

    @Column(length = 30)
    private String phone;

    @Column(length = 60)
    private String country;

    @Column(name = "customer_type", length = 20)
    private String customerType;

    @Column(name = "billing_address", length = 255)
    private String billingAddress;

    @Column(length = 80)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "billing_country", length = 60)
    private String billingCountry;

    @Column(name = "tax_registration_type", length = 30)
    private String taxRegistrationType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "preferred_currency", nullable = false, length = 3)
    private String preferredCurrency;

    @Column(name = "autopay_enabled", nullable = false)
    private boolean autopayEnabled;

    @Column(name = "autopay_method", length = 20)
    private String autopayMethod;

    @Column(name = "autopay_token", length = 120)
    private String autopayToken;

    @Column(name = "gateway_customer_id")
    private String gatewayCustomerId;

    @Column(name = "monthly_budget", precision = 14, scale = 2)
    private java.math.BigDecimal monthlyBudget;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IndianState state;

    @Column(length = 20)
    private String gstin;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (preferredCurrency == null) {
            preferredCurrency = "INR";
        }
    }
}
