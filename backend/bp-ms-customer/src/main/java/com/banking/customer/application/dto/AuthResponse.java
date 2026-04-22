package com.banking.customer.application.dto;

public record AuthResponse(
        String token,
        String customerId,
        String name,
        String role
) {}
