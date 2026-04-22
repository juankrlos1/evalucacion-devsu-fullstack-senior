package com.banking.customer.domain.exception;

/**
 * Thrown when a customer cannot be found by the given identifier.
 */
public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}
