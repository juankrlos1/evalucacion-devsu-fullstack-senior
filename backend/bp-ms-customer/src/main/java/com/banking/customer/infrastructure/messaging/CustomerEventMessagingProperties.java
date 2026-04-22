package com.banking.customer.infrastructure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging.customer-events")
public record CustomerEventMessagingProperties(
        String exchange,
        String routingKey
) {}
