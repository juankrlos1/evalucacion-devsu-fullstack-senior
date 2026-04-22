package com.banking.customer.application.event;

import com.banking.customer.domain.model.Customer;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a customer is created, updated, or deactivated.
 */
public record CustomerEvent(
        String eventId,
        CustomerEventType eventType,
        Instant occurredAt,
        String customerId,
        String name,
        String identification,
        boolean active
) {
    public static CustomerEvent from(CustomerEventType type, Customer customer) {
        return new CustomerEvent(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                customer.getCustomerId(),
                customer.getName(),
                customer.getIdentification(),
                customer.isActive()
        );
    }
}
