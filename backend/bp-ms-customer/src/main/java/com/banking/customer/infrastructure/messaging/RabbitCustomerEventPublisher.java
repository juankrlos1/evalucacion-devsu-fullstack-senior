package com.banking.customer.infrastructure.messaging;

import com.banking.customer.application.event.CustomerEvent;
import com.banking.customer.domain.port.out.CustomerEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public class RabbitCustomerEventPublisher implements CustomerEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final CustomerEventMessagingProperties properties;

    @Override
    public void publish(CustomerEvent event) {
        log.info("Publishing customer event: type={}, customerId={}", event.eventType(), event.customerId());
        rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), event);
    }
}
