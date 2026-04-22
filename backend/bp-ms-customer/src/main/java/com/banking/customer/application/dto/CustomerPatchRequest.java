package com.banking.customer.application.dto;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for partial customer updates. All fields are optional.
 */
public record CustomerPatchRequest(
        String name,
        String gender,
        @PositiveOrZero Integer age,
        String identification,
        String address,
        String phone,
        String password,
        Boolean active
) {}
