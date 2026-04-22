package com.banking.customer.application.dto;

/**
 * Response DTO for customer data. Password is excluded for security.
 */
public record CustomerResponse(
        String customerId,
        String name,
        String gender,
        Integer age,
        String identification,
        String address,
        String phone,
        String role,
        boolean active
) {}
