package com.banking.customer.domain.model;

import com.banking.customer.domain.exception.DomainException;
import lombok.Getter;

/**
 * Aggregate root representing a customer in the banking system.
 * Extends Person with customer-specific fields.
 */
@Getter
public class Customer extends Person {

    private final String customerId;
    private final String password;
    private final String role;
    private final boolean active;

    public Customer(String customerId, String password, String role, boolean active, PersonData data) {
        super(data.name(), data.gender(), data.age(),
              data.identification(), data.address(), data.phone());
        if (customerId == null || customerId.isBlank()) {
            throw new DomainException("Customer ID is required");
        }
        if (password == null || password.isBlank()) {
            throw new DomainException("Password is required");
        }
        this.customerId = customerId.trim();
        this.password = password.trim();
        this.role = role != null ? role.trim() : "USER";
        this.active = active;
    }

    /**
     * Deactivates this customer, returning a new immutable instance.
     */
    public Customer deactivate() {
        return new Customer(
                this.customerId,
                this.password,
                this.role,
                false,
                new PersonData(getName(), getGender(), getAge(),
                        getIdentification(), getAddress(), getPhone())
        );
    }
}
