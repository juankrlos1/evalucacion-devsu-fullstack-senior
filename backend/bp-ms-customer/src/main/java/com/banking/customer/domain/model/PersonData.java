package com.banking.customer.domain.model;

import com.banking.customer.domain.exception.DomainException;

/**
 * Value object holding validated personal data.
 */
public record PersonData(
        String name,
        String gender,
        Integer age,
        String identification,
        String address,
        String phone
) {
    public PersonData {
        if (name == null || name.isBlank()) {
            throw new DomainException("Name is required");
        }
        if (identification == null || identification.isBlank()) {
            throw new DomainException("Identification is required");
        }
        if (address == null || address.isBlank()) {
            throw new DomainException("Address is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new DomainException("Phone is required");
        }
        name = name.trim();
        identification = identification.trim();
        address = address.trim();
        phone = phone.trim();
        gender = gender != null ? gender.trim() : null;
    }
}
