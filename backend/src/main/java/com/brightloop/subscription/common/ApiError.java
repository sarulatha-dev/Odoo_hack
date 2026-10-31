package com.brightloop.subscription.common;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError validation(String path, List<String> messages) {
        Map<String, String> fields = new java.util.LinkedHashMap<>();
        for (int i = 0; i < messages.size(); i++) {
            fields.put("field" + i, messages.get(i));
        }
        return new ApiError(Instant.now(), 400, "Bad Request", "Validation failed", path, fields);
    }
}
