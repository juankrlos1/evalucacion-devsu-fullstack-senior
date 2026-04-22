package com.banking.account.application.usecase;

import com.banking.account.application.dto.AccountPatchRequest;
import com.banking.account.application.dto.AccountRequest;
import com.banking.account.application.dto.AccountResponse;
import com.banking.account.application.mapper.AccountMapper;
import com.banking.account.application.port.in.AccountService;
import com.banking.account.domain.exception.AccountNotFoundException;
import com.banking.account.domain.exception.DuplicateAccountException;
import com.banking.account.domain.model.Account;
import com.banking.account.domain.model.AccountData;
import com.banking.account.domain.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountUseCase implements AccountService {

    private final AccountRepository repository;
    private final AccountMapper mapper;

    @Override
    @Transactional
    public Mono<AccountResponse> create(AccountRequest request) {
        return Mono.fromCallable(() -> {
            String accountNumber = generateAccountNumber();
            Account account = mapper.toDomain(accountNumber, request);
            Account saved = repository.save(account);
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.valueOf(100000 + ThreadLocalRandom.current().nextInt(900000));
        } while (repository.existsByAccountNumber(number));
        return number;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AccountResponse> get(String accountNumber) {
        return Mono.fromCallable(() -> {
            Account account = repository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            return mapper.toResponse(account);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AccountResponse> list() {
        return Mono.fromCallable(repository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AccountResponse> listByCustomerId(String customerId) {
        return Mono.fromCallable(() -> repository.findByCustomerId(customerId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<AccountResponse> replace(String accountNumber, AccountRequest request) {
        return Mono.fromCallable(() -> {
            if (!repository.existsByAccountNumber(accountNumber)) {
                throw new AccountNotFoundException(accountNumber);
            }
            var data = new AccountData(
                    request.accountType(), request.initialBalance(), request.initialBalance(),
                    request.active(), request.customerId()
            );
            Account account = new Account(accountNumber, data);
            Account saved = repository.save(account);
            log.info("Account replaced: number={}", accountNumber);
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<AccountResponse> patch(String accountNumber, AccountPatchRequest request) {
        return Mono.fromCallable(() -> {
            Account existing = repository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            Account updated = existing.updateData(
                    request.accountType(), request.active(), request.customerId()
            );
            Account saved = repository.save(updated);
            log.info("Account patched: number={}", accountNumber);
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<Void> delete(String accountNumber) {
        return Mono.fromCallable(() -> {
            Account existing = repository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(accountNumber));
            Account deactivated = existing.updateData(null, false, null);
            repository.save(deactivated);
            log.info("Account deactivated: number={}", accountNumber);
            return deactivated;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
