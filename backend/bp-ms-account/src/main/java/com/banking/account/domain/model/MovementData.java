package com.banking.account.domain.model;

import com.banking.account.domain.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Value object holding validated movement data.
 */
public record MovementData(
        String accountNumber,
        LocalDateTime date,
        String movementType,
        BigDecimal value,
        BigDecimal balance
) {
    public MovementData {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new DomainException("Account number is required for movement");
        }
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            throw new DomainException("Movement value cannot be zero");
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Balance must be non-negative");
        }
        if (date == null) {
            date = LocalDateTime.now();
        }
        accountNumber = accountNumber.trim();

        movementType = value.compareTo(BigDecimal.ZERO) < 0 ? "Withdrawal" : "Deposit";
    }
}
