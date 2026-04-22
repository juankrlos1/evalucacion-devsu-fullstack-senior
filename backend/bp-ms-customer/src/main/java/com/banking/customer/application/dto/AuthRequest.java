package com.banking.customer.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String identification,
        @NotBlank String password
) {}
