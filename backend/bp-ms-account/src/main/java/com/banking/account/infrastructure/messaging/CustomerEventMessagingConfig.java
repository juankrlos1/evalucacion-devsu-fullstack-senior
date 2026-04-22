package com.banking.account.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CustomerEventMessagingProperties.class)
@ConditionalOnBean(RabbitTemplate.class)
public class CustomerEventMessagingConfig {

    @Bean
    public DirectExchange customerEventsExchange(CustomerEventMessagingProperties props) {
        return new DirectExchange(props.exchange(), true, false);
    }

    @Bean
    public Queue customerEventsQueue(CustomerEventMessagingProperties props) {
        return new Queue(props.queue(), true);
    }

    @Bean
    public Binding customerEventsBinding(Queue customerEventsQueue, DirectExchange customerEventsExchange,
                                          CustomerEventMessagingProperties props) {
        return BindingBuilder.bind(customerEventsQueue).to(customerEventsExchange).with(props.routingKey());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
