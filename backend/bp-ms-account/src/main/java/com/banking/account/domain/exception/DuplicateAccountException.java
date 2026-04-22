package com.banking.account.domain.exception;

public class DuplicateAccountException extends DomainException {

    public DuplicateAccountException(String accountNumber) {
        super("Account already exists: " + accountNumber);
    }
}
