package com.banking.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Customer Microservice — customer CRUD with domain events via RabbitMQ.
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@SpringBootApplication
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
