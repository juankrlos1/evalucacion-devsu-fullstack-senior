package com.banking.account.application.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String accountNumber,
        String accountType,
        BigDecimal initialBalance,
        BigDecimal availableBalance,
        boolean active,
        String customerId
) {}
