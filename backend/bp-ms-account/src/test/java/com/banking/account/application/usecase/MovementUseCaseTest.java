package com.banking.account.application.usecase;

import com.banking.account.application.dto.MovementRequest;
import com.banking.account.application.dto.MovementResponse;
import com.banking.account.application.mapper.MovementMapper;
import com.banking.account.domain.exception.AccountNotFoundException;
import com.banking.account.domain.exception.DailyLimitExceededException;
import com.banking.account.domain.exception.InsufficientBalanceException;
import com.banking.account.domain.model.Account;
import com.banking.account.domain.model.AccountData;
import com.banking.account.domain.model.Movement;
import com.banking.account.domain.model.MovementData;
import com.banking.account.domain.port.out.AccountRepository;
import com.banking.account.domain.port.out.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementUseCaseTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementMapper mapper;

    @InjectMocks
    private MovementUseCase movementUseCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(movementUseCase, "dailyWithdrawalLimit", new BigDecimal("1000"));
    }

    @Test
    @DisplayName("Should create a deposit movement successfully")
    void shouldCreateDepositMovement() {
        var account = new Account("478758", new AccountData("Savings",
                new BigDecimal("2000"), new BigDecimal("2000"), true, "CLI-001"));
        var updatedAccount = account.applyMovement(new BigDecimal("500"));
        var movement = new Movement("mv-1", new MovementData("478758",
                LocalDateTime.now(), null, new BigDecimal("500"), new BigDecimal("2500")));
        var response = new MovementResponse("mv-1", "478758", LocalDateTime.now(),
                "Deposit", new BigDecimal("500"), new BigDecimal("2500"));

        when(accountRepository.findByAccountNumber("478758")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
        when(mapper.toDomain(eq("478758"), eq(new BigDecimal("500")), any())).thenReturn(movement);
        when(movementRepository.save(movement)).thenReturn(movement);
        when(mapper.toResponse(movement)).thenReturn(response);

        var request = new MovementRequest("478758", new BigDecimal("500"));

        StepVerifier.create(movementUseCase.create(request))
                .assertNext(r -> {
                    assertThat(r.movementType()).isEqualTo("Deposit");
                    assertThat(r.balance()).isEqualByComparingTo(new BigDecimal("2500"));
                })
                .verifyComplete();

        verify(accountRepository).save(any(Account.class));
        verify(movementRepository).save(movement);
    }

    @Test
    @DisplayName("Should create a withdrawal movement successfully")
    void shouldCreateWithdrawalMovement() {
        var account = new Account("478758", new AccountData("Savings",
                new BigDecimal("2000"), new BigDecimal("2000"), true, "CLI-001"));
        var updatedAccount = account.applyMovement(new BigDecimal("-575"));
        var movement = new Movement("mv-2", new MovementData("478758",
                LocalDateTime.now(), null, new BigDecimal("-575"), new BigDecimal("1425")));
        var response = new MovementResponse("mv-2", "478758", LocalDateTime.now(),
                "Withdrawal", new BigDecimal("-575"), new BigDecimal("1425"));

        when(accountRepository.findByAccountNumber("478758")).thenReturn(Optional.of(account));
        when(movementRepository.sumDailyWithdrawals(eq("478758"), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);
        when(mapper.toDomain(eq("478758"), eq(new BigDecimal("-575")), any())).thenReturn(movement);
        when(movementRepository.save(movement)).thenReturn(movement);
        when(mapper.toResponse(movement)).thenReturn(response);

        var request = new MovementRequest("478758", new BigDecimal("-575"));

        StepVerifier.create(movementUseCase.create(request))
                .assertNext(r -> {
                    assertThat(r.movementType()).isEqualTo("Withdrawal");
                    assertThat(r.balance()).isEqualByComparingTo(new BigDecimal("1425"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should reject withdrawal when balance is insufficient")
    void shouldRejectInsufficientBalance() {
        var account = new Account("478758", new AccountData("Savings",
                new BigDecimal("100"), new BigDecimal("100"), true, "CLI-001"));

        when(accountRepository.findByAccountNumber("478758")).thenReturn(Optional.of(account));

        var request = new MovementRequest("478758", new BigDecimal("-200"));

        StepVerifier.create(movementUseCase.create(request))
                .expectErrorMatches(e -> e instanceof InsufficientBalanceException
                        && e.getMessage().equals("Saldo no disponible"))
                .verify();
    }

    @Test
    @DisplayName("Should reject withdrawal when daily limit exceeded")
    void shouldRejectDailyLimitExceeded() {
        var account = new Account("478758", new AccountData("Savings",
                new BigDecimal("5000"), new BigDecimal("5000"), true, "CLI-001"));

        when(accountRepository.findByAccountNumber("478758")).thenReturn(Optional.of(account));
        when(movementRepository.sumDailyWithdrawals(eq("478758"), any(), any()))
                .thenReturn(new BigDecimal("900"));

        var request = new MovementRequest("478758", new BigDecimal("-200"));

        StepVerifier.create(movementUseCase.create(request))
                .expectErrorMatches(e -> e instanceof DailyLimitExceededException
                        && e.getMessage().equals("Cupo diario Excedido"))
                .verify();
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException for non-existent account")
    void shouldRejectNonExistentAccount() {
        when(accountRepository.findByAccountNumber("NONEXISTENT")).thenReturn(Optional.empty());

        var request = new MovementRequest("NONEXISTENT", new BigDecimal("100"));

        StepVerifier.create(movementUseCase.create(request))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should get movement by ID")
    void shouldGetMovementById() {
        var movement = new Movement("mv-1", new MovementData("478758",
                LocalDateTime.now(), null, new BigDecimal("500"), new BigDecimal("2500")));
        var response = new MovementResponse("mv-1", "478758", LocalDateTime.now(),
                "Deposit", new BigDecimal("500"), new BigDecimal("2500"));

        when(movementRepository.findByMovementId("mv-1")).thenReturn(Optional.of(movement));
        when(mapper.toResponse(movement)).thenReturn(response);

        StepVerifier.create(movementUseCase.get("mv-1"))
                .assertNext(r -> assertThat(r.movementId()).isEqualTo("mv-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete movement and reverse balance")
    void shouldDeleteMovementAndReverseBalance() {
        var movement = new Movement("mv-1", new MovementData("478758",
                LocalDateTime.now(), null, new BigDecimal("-500"), new BigDecimal("1500")));
        var account = new Account("478758", new AccountData("Savings",
                new BigDecimal("2000"), new BigDecimal("1500"), true, "CLI-001"));

        when(movementRepository.findByMovementId("mv-1")).thenReturn(Optional.of(movement));
        when(accountRepository.findByAccountNumber("478758")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(movementUseCase.delete("mv-1"))
                .verifyComplete();

        verify(movementRepository).deleteByMovementId("mv-1");
        verify(accountRepository).save(argThat(a ->
                a.getAvailableBalance().compareTo(new BigDecimal("2000")) == 0));
    }
}

