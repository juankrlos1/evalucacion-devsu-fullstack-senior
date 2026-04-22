package com.banking.account.domain.model;

import com.banking.account.domain.exception.DomainException;

import java.math.BigDecimal;

/**
 * Value object holding validated account data.
 */
public record AccountData(
        String accountType,
        BigDecimal initialBalance,
        BigDecimal availableBalance,
        boolean active,
        String customerId
) {
    public AccountData {
        if (accountType == null || accountType.isBlank()) {
            throw new DomainException("Account type is required");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Initial balance must be non-negative");
        }
        if (availableBalance == null || availableBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Available balance must be non-negative");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new DomainException("Customer ID is required");
        }
        accountType = accountType.trim();
        customerId = customerId.trim();
    }
}
