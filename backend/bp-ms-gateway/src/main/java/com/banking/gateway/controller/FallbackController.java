package com.banking.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Fallback controller for circuit breaker scenarios.
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping(value = "/unavailable", method = {
            RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.PATCH,
            RequestMethod.DELETE
    })
    public Mono<Map<String, Object>> serviceUnavailable(ServerWebExchange exchange) {
        log.warn("Circuit breaker fallback triggered: {} {}",
                exchange.getRequest().getMethod(), exchange.getRequest().getURI().getPath());
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just(Map.of(
                "success", false,
                "message", "Service temporarily unavailable. Please try again later.",
                "data", Map.of()
        ));
    }
}

