package com.banking.account.infrastructure.web;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(
        Instant timestamp,
        boolean success,
        String message,
        T data,
        Map<String, String> errors
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(Instant.now(), true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(Instant.now(), false, message, null, errors);
    }
}
