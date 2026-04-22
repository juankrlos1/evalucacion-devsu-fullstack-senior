package com.banking.account.domain.exception;

/**
 * Thrown when attempting a debit transaction with insufficient balance.
 */
public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException() {
        super("Saldo no disponible");
    }
}
