package com.banking.account.domain.exception;

public class AccountNotFoundException extends DomainException {

    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber);
    }
}
