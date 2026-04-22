package com.banking.customer.infrastructure.messaging;

import com.banking.customer.domain.port.out.CustomerEventPublisher;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CustomerEventMessagingProperties.class)
public class CustomerEventMessagingConfig {

    @Configuration
    @ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true", matchIfMissing = true)
    static class RabbitConfig {
        @Bean
        public DirectExchange customerEventsExchange(CustomerEventMessagingProperties props) {
            return new DirectExchange(props.exchange(), true, false);
        }

        @Bean
        public MessageConverter jsonMessageConverter() {
            return new Jackson2JsonMessageConverter();
        }

        @Bean
        public CustomerEventPublisher rabbitCustomerEventPublisher(RabbitTemplate rabbitTemplate,
                                                                    CustomerEventMessagingProperties props) {
            return new RabbitCustomerEventPublisher(rabbitTemplate, props);
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "false")
    static class NoOpConfig {
        @Bean
        public CustomerEventPublisher noOpCustomerEventPublisher() {
            return event -> {};
        }
    }
}
