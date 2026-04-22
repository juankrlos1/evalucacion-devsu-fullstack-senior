package com.banking.gateway.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final Cache<String, AtomicInteger> requestCounts;
    private final int maxRequests;

    public RateLimitFilter(
            @Value("${gateway.rate-limit.requests-per-window:100}") int maxRequests,
            @Value("${gateway.rate-limit.window-seconds:60}") int windowSeconds
    ) {
        this.maxRequests = maxRequests;
        this.requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(windowSeconds))
                .maximumSize(10_000)
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = resolveClientIp(exchange);
        AtomicInteger counter = requestCounts.get(clientIp, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(maxRequests));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, maxRequests - currentCount)));

        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String body = """
                    {"success":false,"message":"Too many requests. Please try again later.","data":null}\
                    """;
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .orElse("unknown");
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
