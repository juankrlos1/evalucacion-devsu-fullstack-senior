package com.banking.account.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(RabbitTemplate.class)
public class CustomerEventListener {

    @RabbitListener(queues = "${app.messaging.customer-events.queue}")
    public void handleCustomerEvent(CustomerEventMessage event) {
        log.info("Received customer event: type={}, customerId={}, name={}, active={}",
                event.eventType(), event.customerId(), event.name(), event.active());
    }
}
