package com.banking.account.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single row in the account statement report.
 */
public record AccountStatementResponse(
        LocalDate date,
        String customer,
        String accountNumber,
        String accountType,
        BigDecimal initialBalance,
        boolean active,
        BigDecimal movement,
        BigDecimal availableBalance
) {}
