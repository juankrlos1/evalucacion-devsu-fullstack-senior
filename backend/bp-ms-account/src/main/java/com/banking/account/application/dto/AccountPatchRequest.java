package com.banking.account.application.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountPatchRequest(
        String accountType,
        @PositiveOrZero BigDecimal initialBalance,
        Boolean active,
        String customerId
) {}
