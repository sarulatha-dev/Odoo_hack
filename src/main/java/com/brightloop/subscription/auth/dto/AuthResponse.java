package com.brightloop.subscription.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMinutes,
        UserSummary user) {
}
