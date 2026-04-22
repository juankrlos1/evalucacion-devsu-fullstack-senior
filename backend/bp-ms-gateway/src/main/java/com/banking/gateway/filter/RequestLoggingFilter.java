package com.banking.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant start = Instant.now();

        String clientIp = Optional.ofNullable(request.getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(java.net.InetAddress::getHostAddress)
                .orElse("unknown");

        String requestId = (String) exchange.getAttributes().getOrDefault("requestId", "N/A");

        log.info("[{}] --> {} {} from {}", requestId, request.getMethod(), request.getURI().getPath(), clientIp);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            int statusCode = Optional.ofNullable(exchange.getResponse().getStatusCode())
                    .map(HttpStatusCode::value)
                    .orElse(0);
            log.info("[{}] <-- {} {} {}ms", requestId, statusCode, request.getURI().getPath(), durationMs);
        }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
