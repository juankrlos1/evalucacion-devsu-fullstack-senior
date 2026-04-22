package com.banking.account.domain.exception;

public class MovementNotFoundException extends DomainException {

    public MovementNotFoundException(String movementId) {
        super("Movement not found: " + movementId);
    }
}
