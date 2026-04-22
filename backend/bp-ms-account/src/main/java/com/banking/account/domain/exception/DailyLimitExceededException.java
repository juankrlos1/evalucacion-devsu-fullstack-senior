package com.banking.account.domain.exception;

/**
 * Thrown when daily withdrawal limit ($1000) has been exceeded.
 */
public class DailyLimitExceededException extends DomainException {

    public DailyLimitExceededException() {
        super("Cupo diario Excedido");
    }
}
