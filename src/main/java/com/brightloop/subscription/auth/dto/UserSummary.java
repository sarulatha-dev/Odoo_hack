package com.brightloop.subscription.auth.dto;

import java.math.BigDecimal;

import com.brightloop.subscription.user.User;

public record UserSummary(
        Long id,
        String email,
        String fullName,
        String role,
        String state,
        String gstin,
        String preferredCurrency,
        boolean autopayEnabled,
        String autopayMethod,
        BigDecimal monthlyBudget) {

    public static UserSummary from(User user) {
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getState() == null ? null : user.getState().label(),
                user.getGstin(),
                user.getPreferredCurrency(),
                user.isAutopayEnabled(),
                user.getAutopayMethod(),
                user.getMonthlyBudget());
    }
}
