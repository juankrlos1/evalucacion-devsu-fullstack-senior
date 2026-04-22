package com.banking.customer.application.usecase;

import com.banking.customer.application.dto.AuthRequest;
import com.banking.customer.application.dto.AuthResponse;
import com.banking.customer.application.port.in.AuthService;
import com.banking.customer.domain.exception.DomainException;
import com.banking.customer.domain.model.Customer;
import com.banking.customer.domain.port.out.CustomerRepository;
import com.banking.customer.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Authentication use case — validates credentials and issues JWT tokens.
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUseCase implements AuthService {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional(readOnly = true)
    public Mono<AuthResponse> login(AuthRequest request) {
        return Mono.fromCallable(() -> {
            Customer customer = repository.findByIdentification(request.identification())
                    .orElseThrow(() -> new DomainException("Invalid credentials"));

            if (!customer.isActive()) {
                throw new DomainException("Account is deactivated");
            }

            if (!passwordEncoder.matches(request.password(), customer.getPassword())) {
                throw new DomainException("Invalid credentials");
            }

            String token = jwtProvider.generateToken(
                    customer.getCustomerId(), customer.getName(), customer.getRole());

            log.info("Login successful: customerId={}, role={}", customer.getCustomerId(), customer.getRole());

            return new AuthResponse(token, customer.getCustomerId(), customer.getName(), customer.getRole());
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
