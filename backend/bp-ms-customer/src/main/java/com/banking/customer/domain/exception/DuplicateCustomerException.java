package com.banking.customer.domain.exception;

/**
 * Thrown when attempting to create a customer with a duplicate ID or identification.
 */
public class DuplicateCustomerException extends DomainException {

    public DuplicateCustomerException(String message) {
        super(message);
    }
}
