package com.banking.customer.application.port.in;

import com.banking.customer.application.dto.AuthRequest;
import com.banking.customer.application.dto.AuthResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<AuthResponse> login(AuthRequest request);
}
