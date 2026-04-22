package com.banking.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class RequestValidationFilter implements GlobalFilter, Ordered {

    private static final Set<HttpMethod> BODY_METHODS = Set.of(
            HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        if (method != null && BODY_METHODS.contains(method)) {
            MediaType contentType = exchange.getRequest().getHeaders().getContentType();
            if (contentType == null || !contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
