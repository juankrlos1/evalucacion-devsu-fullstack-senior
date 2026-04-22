package com.banking.customer.application.usecase;

import com.banking.customer.application.dto.CustomerPatchRequest;
import com.banking.customer.application.dto.CustomerRequest;
import com.banking.customer.application.dto.CustomerResponse;
import com.banking.customer.application.event.CustomerEvent;
import com.banking.customer.application.event.CustomerEventType;
import com.banking.customer.application.mapper.CustomerMapper;
import com.banking.customer.application.port.in.CustomerService;
import com.banking.customer.domain.exception.CustomerNotFoundException;
import com.banking.customer.domain.exception.DuplicateCustomerException;
import com.banking.customer.domain.model.Customer;
import com.banking.customer.domain.port.out.CustomerEventPublisher;
import com.banking.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerUseCase implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final CustomerEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Mono<CustomerResponse> create(CustomerRequest request) {
        return Mono.fromCallable(() -> {
            repository.findByIdentification(request.identification()).ifPresent(c -> {
                throw new DuplicateCustomerException("Identification already exists: " + request.identification());
            });
            String customerId = generateCustomerId();
            Customer customer = mapper.toDomain(customerId, request);
            Customer withHash = hashPassword(customer);
            Customer saved = repository.save(withHash);
            eventPublisher.publish(CustomerEvent.from(CustomerEventType.CUSTOMER_CREATED, saved));
            log.info("Customer created: id={}, identification={}", saved.getCustomerId(), saved.getIdentification());
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String generateCustomerId() {
        String id;
        do {
            id = "CLI-" + UUID.randomUUID().toString().substring(0, 8);
        } while (repository.existsByCustomerId(id));
        return id;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<CustomerResponse> get(String customerId) {
        return Mono.fromCallable(() -> {
            Customer customer = repository.findByCustomerId(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException(customerId));
            return mapper.toResponse(customer);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<CustomerResponse> list() {
        return Mono.fromCallable(() -> repository.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<CustomerResponse> replace(String customerId, CustomerRequest request) {
        return Mono.fromCallable(() -> {
            Customer existing = repository.findByCustomerId(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException(customerId));
            Customer updated = mapper.toDomain(customerId, request);
            Customer withHash = hashPassword(updated);
            Customer saved = repository.save(withHash);
            eventPublisher.publish(CustomerEvent.from(CustomerEventType.CUSTOMER_UPDATED, saved));
            log.info("Customer replaced: id={}", customerId);
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<CustomerResponse> patch(String customerId, CustomerPatchRequest request) {
        return Mono.fromCallable(() -> {
            Customer existing = repository.findByCustomerId(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException(customerId));
            Customer patched = mapper.mergePatch(request, existing);
            Customer withHash = hashPasswordIfChanged(patched, existing);
            Customer saved = repository.save(withHash);
            eventPublisher.publish(CustomerEvent.from(CustomerEventType.CUSTOMER_UPDATED, saved));
            log.info("Customer patched: id={}", customerId);
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<Void> delete(String customerId) {
        return Mono.fromCallable(() -> {
            Customer existing = repository.findByCustomerId(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException(customerId));
            Customer deactivated = existing.deactivate();
            repository.save(deactivated);
            eventPublisher.publish(CustomerEvent.from(CustomerEventType.CUSTOMER_DEACTIVATED, deactivated));
            log.info("Customer deactivated: id={}", customerId);
            return deactivated;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private Customer hashPassword(Customer customer) {
        String hashed = passwordEncoder.encode(customer.getPassword());
        return new Customer(customer.getCustomerId(), hashed, customer.getRole(), customer.isActive(),
                new com.banking.customer.domain.model.PersonData(
                        customer.getName(), customer.getGender(), customer.getAge(),
                        customer.getIdentification(), customer.getAddress(), customer.getPhone()));
    }

    private Customer hashPasswordIfChanged(Customer patched, Customer existing) {
        if (!patched.getPassword().equals(existing.getPassword())) {
            return hashPassword(patched);
        }
        return patched;
    }
}
