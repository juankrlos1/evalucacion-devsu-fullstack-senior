package com.banking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — routes, circuit breaker, rate limiting, security headers.
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

