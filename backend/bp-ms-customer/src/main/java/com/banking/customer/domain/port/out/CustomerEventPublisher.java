package com.banking.customer.domain.port.out;

import com.banking.customer.application.event.CustomerEvent;

/**
 * Output port for publishing customer domain events.
 */
public interface CustomerEventPublisher {

    void publish(CustomerEvent event);
}
