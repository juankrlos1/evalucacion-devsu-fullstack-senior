package com.banking.account.domain.model;

import com.banking.account.domain.exception.DomainException;
import com.banking.account.domain.exception.InsufficientBalanceException;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Aggregate root representing a bank account.
 */
@Getter
public class Account {

    private final String accountNumber;
    private final AccountData data;

    public Account(String accountNumber, AccountData data) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new DomainException("Account number is required");
        }
        this.accountNumber = accountNumber.trim();
        this.data = data;
    }

    public String getAccountType() { return data.accountType(); }
    public BigDecimal getInitialBalance() { return data.initialBalance(); }
    public BigDecimal getAvailableBalance() { return data.availableBalance(); }
    public boolean isActive() { return data.active(); }
    public String getCustomerId() { return data.customerId(); }

    /**
     * Applies a movement (deposit or withdrawal) and returns a new Account with updated balance.
     * Positive value = deposit, negative value = withdrawal.
     */
    public Account applyMovement(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            throw new DomainException("Movement value cannot be zero");
        }
        BigDecimal newBalance = data.availableBalance().add(value);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException();
        }
        var newData = new AccountData(
                data.accountType(), data.initialBalance(), newBalance,
                data.active(), data.customerId()
        );
        return new Account(this.accountNumber, newData);
    }

    /**
     * Adjusts balance by replacing an old movement value with a new one.
     */
    public Account adjustMovement(BigDecimal oldValue, BigDecimal newValue) {
        BigDecimal delta = newValue.subtract(oldValue);
        BigDecimal newBalance = data.availableBalance().add(delta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException();
        }
        var newData = new AccountData(
                data.accountType(), data.initialBalance(), newBalance,
                data.active(), data.customerId()
        );
        return new Account(this.accountNumber, newData);
    }

    /**
     * Updates account metadata (not balance-related).
     */
    public Account updateData(String accountType, Boolean active, String customerId) {
        var newData = new AccountData(
                accountType != null ? accountType : data.accountType(),
                data.initialBalance(),
                data.availableBalance(),
                active != null ? active : data.active(),
                customerId != null ? customerId : data.customerId()
        );
        return new Account(this.accountNumber, newData);
    }
}
