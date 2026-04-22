package com.banking.customer.infrastructure.web;

import com.banking.customer.application.dto.AuthRequest;
import com.banking.customer.application.dto.AuthResponse;
import com.banking.customer.application.port.in.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y obtención de JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y retorna un token JWT")
    public Mono<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request)
                .map(r -> ApiResponse.success("Login successful", r));
    }
}
