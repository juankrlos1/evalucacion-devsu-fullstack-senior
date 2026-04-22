package com.banking.account.infrastructure.messaging;

import java.time.Instant;

public record CustomerEventMessage(
        String eventId,
        String eventType,
        Instant occurredAt,
        String customerId,
        String name,
        String identification,
        boolean active
) {}
