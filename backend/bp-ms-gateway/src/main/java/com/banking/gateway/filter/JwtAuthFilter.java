package com.banking.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Gateway filter that validates JWT tokens and enforces role-based access.
 * Public paths (auth, actuator) are excluded.
 * Injects X-User-Id and X-User-Role headers into downstream requests.
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/actuator/health",
            "/actuator/info"
    );

    private final SecretKey key;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Encabezado de autorización faltante o inválido");
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (!checkRoleAccess(path, role, exchange.getRequest().getMethod())) {
                log.warn("Access denied: user={} role={} path={}", userId, role, path);
                return forbidden(exchange, "No tiene permisos para acceder a este recurso");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());
            headers.set("X-User-Id", userId);
            headers.set("X-User-Role", role);

            ServerHttpRequest decorated = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }
            };

            return chain.filter(exchange.mutate().request(decorated).build());

        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return unauthorized(exchange, "Token inválido o expirado");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean checkRoleAccess(String path, String role, HttpMethod method) {
        if ("ADMIN".equals(role)) {
            return true;
        }

        if (path.startsWith("/api/clientes") && method != HttpMethod.GET) {
            return false;
        }
        return true;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"success":false,"message":"%s","data":null}\
                """.formatted(message);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -5; // After MutableHeadersFilter(-10), before RateLimitFilter(-2)
    }
}
