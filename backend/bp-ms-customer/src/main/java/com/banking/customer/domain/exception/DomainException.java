package com.banking.customer.domain.exception;

/**
 * Base domain exception for business rule violations.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
