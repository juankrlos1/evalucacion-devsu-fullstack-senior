package com.banking.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Global error handler that returns clean JSON responses instead of stack traces.
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@Slf4j
@Component
@Order(-2)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(status, ex);

        log.error("Gateway error: {} {} → {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                status.value(), message);

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"success":false,"message":"%s","data":null}""".formatted(
                message.replace("\"", "\\\""));

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof ConnectException || cause instanceof java.net.UnknownHostException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (cause instanceof TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status, Throwable ex) {
        return switch (status) {
            case SERVICE_UNAVAILABLE -> "Servicio no disponible. Intente nuevamente.";
            case GATEWAY_TIMEOUT -> "Tiempo de espera agotado. Intente nuevamente.";
            case NOT_FOUND -> "Recurso no encontrado";
            case UNAUTHORIZED -> "No autorizado";
            case FORBIDDEN -> "Acceso denegado";
            default -> "Error interno del servidor";
        };
    }
}
