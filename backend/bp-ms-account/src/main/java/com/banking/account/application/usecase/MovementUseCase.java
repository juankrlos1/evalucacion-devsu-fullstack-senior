package com.banking.account.application.usecase;

import com.banking.account.application.dto.MovementPatchRequest;
import com.banking.account.application.dto.MovementRequest;
import com.banking.account.application.dto.MovementResponse;
import com.banking.account.application.mapper.MovementMapper;
import com.banking.account.application.port.in.MovementService;
import com.banking.account.domain.exception.AccountNotFoundException;
import com.banking.account.domain.exception.DailyLimitExceededException;
import com.banking.account.domain.exception.MovementNotFoundException;
import com.banking.account.domain.model.Account;
import com.banking.account.domain.model.Movement;
import com.banking.account.domain.port.out.AccountRepository;
import com.banking.account.domain.port.out.MovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementUseCase implements MovementService {

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final MovementMapper mapper;

    @Value("${app.business.daily-withdrawal-limit:1000}")
    private BigDecimal dailyWithdrawalLimit;

    @Override
    @Transactional
    public Mono<MovementResponse> create(MovementRequest request) {
        return Mono.fromCallable(() -> {
            Account account = accountRepository.findByAccountNumber(request.accountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(request.accountNumber()));

            BigDecimal value = request.value();

            Account updatedAccount = account.applyMovement(value);

            if (value.compareTo(BigDecimal.ZERO) < 0) {
                validateDailyWithdrawalLimit(request.accountNumber(), value);
            }
            accountRepository.save(updatedAccount);

            Movement movement = mapper.toDomain(
                    request.accountNumber(), value, updatedAccount.getAvailableBalance()
            );
            Movement saved = movementRepository.save(movement);
            log.info("Movement created: id={}, account={}, type={}, value={}",
                    saved.getMovementId(), request.accountNumber(), saved.getMovementType(), value);
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void validateDailyWithdrawalLimit(String accountNumber, BigDecimal newWithdrawal) {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        BigDecimal totalWithdrawn = movementRepository.sumDailyWithdrawals(accountNumber, dayStart, dayEnd);
        BigDecimal withdrawalAmount = newWithdrawal.abs();

        if (totalWithdrawn.add(withdrawalAmount).compareTo(dailyWithdrawalLimit) > 0) {
            log.warn("Daily withdrawal limit exceeded: account={}, todayTotal={}, attempted={}, limit={}",
                    accountNumber, totalWithdrawn, withdrawalAmount, dailyWithdrawalLimit);
            throw new DailyLimitExceededException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<MovementResponse> get(String movementId) {
        return Mono.fromCallable(() -> {
            Movement movement = movementRepository.findByMovementId(movementId)
                    .orElseThrow(() -> new MovementNotFoundException(movementId));
            return mapper.toResponse(movement);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<MovementResponse> list() {
        return Mono.fromCallable(movementRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<MovementResponse> listByAccountNumbers(List<String> accountNumbers) {
        return Mono.fromCallable(() -> movementRepository.findByAccountNumbers(accountNumbers))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<MovementResponse> replace(String movementId, MovementRequest request) {
        return Mono.fromCallable(() -> {
            Movement existing = movementRepository.findByMovementId(movementId)
                    .orElseThrow(() -> new MovementNotFoundException(movementId));

            Account account = accountRepository.findByAccountNumber(existing.getAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(existing.getAccountNumber()));

            Account adjusted = account.adjustMovement(existing.getValue(), request.value());
            accountRepository.save(adjusted);

            Movement updated = existing.update(LocalDateTime.now(), request.value(), adjusted.getAvailableBalance());
            Movement saved = movementRepository.save(updated);
            log.info("Movement replaced: id={}, newValue={}", movementId, request.value());
            return mapper.toResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<MovementResponse> patch(String movementId, MovementPatchRequest request) {
        return Mono.fromCallable(() -> {
            Movement existing = movementRepository.findByMovementId(movementId)
                    .orElseThrow(() -> new MovementNotFoundException(movementId));

            if (request.value() != null) {
                Account account = accountRepository.findByAccountNumber(existing.getAccountNumber())
                        .orElseThrow(() -> new AccountNotFoundException(existing.getAccountNumber()));

                Account adjusted = account.adjustMovement(existing.getValue(), request.value());
                accountRepository.save(adjusted);

                Movement updated = existing.update(existing.getDate(), request.value(), adjusted.getAvailableBalance());
                Movement saved = movementRepository.save(updated);
                return mapper.toResponse(saved);
            }

            return mapper.toResponse(existing);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<Void> delete(String movementId) {
        return Mono.fromCallable(() -> {
            Movement existing = movementRepository.findByMovementId(movementId)
                    .orElseThrow(() -> new MovementNotFoundException(movementId));

            Account account = accountRepository.findByAccountNumber(existing.getAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(existing.getAccountNumber()));
            Account adjusted = account.adjustMovement(existing.getValue(), BigDecimal.ZERO);
            accountRepository.save(adjusted);

            movementRepository.deleteByMovementId(movementId);
            log.info("Movement deleted: id={}, reversedValue={}", movementId, existing.getValue());
            return existing;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
