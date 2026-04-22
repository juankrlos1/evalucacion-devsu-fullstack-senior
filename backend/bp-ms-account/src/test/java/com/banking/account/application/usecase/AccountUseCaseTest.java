package com.banking.account.application.usecase;

import com.banking.account.application.dto.AccountRequest;
import com.banking.account.application.dto.AccountResponse;
import com.banking.account.application.mapper.AccountMapper;
import com.banking.account.domain.exception.AccountNotFoundException;
import com.banking.account.domain.model.Account;
import com.banking.account.domain.model.AccountData;
import com.banking.account.domain.port.out.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountUseCaseTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private AccountMapper mapper;

    @InjectMocks
    private AccountUseCase accountUseCase;

    private Account createAccount(String number, BigDecimal balance) {
        return new Account(number, new AccountData("Savings", balance, balance, true, "CLI-001"));
    }

    @Test
    @DisplayName("Should create account successfully")
    void shouldCreateAccount() {
        var request = new AccountRequest("Savings", new BigDecimal("2000"), true, "CLI-001");
        var account = createAccount("478758", new BigDecimal("2000"));
        var response = new AccountResponse("478758", "Savings", new BigDecimal("2000"),
                new BigDecimal("2000"), true, "CLI-001");

        when(repository.existsByAccountNumber(anyString())).thenReturn(false);
        when(mapper.toDomain(anyString(), eq(request))).thenReturn(account);
        when(repository.save(account)).thenReturn(account);
        when(mapper.toResponse(account)).thenReturn(response);

        StepVerifier.create(accountUseCase.create(request))
                .assertNext(r -> {
                    assertThat(r.availableBalance()).isEqualByComparingTo(new BigDecimal("2000"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should generate unique account number")
    void shouldGenerateUniqueAccountNumber() {
        var request = new AccountRequest("Savings", new BigDecimal("2000"), true, "CLI-001");
        var account = createAccount("123456", new BigDecimal("2000"));
        var response = new AccountResponse("123456", "Savings", new BigDecimal("2000"),
                new BigDecimal("2000"), true, "CLI-001");

        when(repository.existsByAccountNumber(anyString())).thenReturn(false);
        when(mapper.toDomain(anyString(), eq(request))).thenReturn(account);
        when(repository.save(account)).thenReturn(account);
        when(mapper.toResponse(account)).thenReturn(response);

        StepVerifier.create(accountUseCase.create(request))
                .assertNext(r -> assertThat(r.accountNumber()).isNotBlank())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get account by number")
    void shouldGetAccount() {
        var account = createAccount("478758", new BigDecimal("2000"));
        var response = new AccountResponse("478758", "Savings", new BigDecimal("2000"),
                new BigDecimal("2000"), true, "CLI-001");

        when(repository.findByAccountNumber("478758")).thenReturn(Optional.of(account));
        when(mapper.toResponse(account)).thenReturn(response);

        StepVerifier.create(accountUseCase.get("478758"))
                .assertNext(r -> assertThat(r.accountNumber()).isEqualTo("478758"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw 404 for non-existent account")
    void shouldThrow404ForNonExistentAccount() {
        when(repository.findByAccountNumber("NOPE")).thenReturn(Optional.empty());

        StepVerifier.create(accountUseCase.get("NOPE"))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should list all accounts")
    void shouldListAccounts() {
        var a1 = createAccount("111", new BigDecimal("1000"));
        var a2 = createAccount("222", new BigDecimal("2000"));
        var r1 = new AccountResponse("111", "Savings", new BigDecimal("1000"),
                new BigDecimal("1000"), true, "CLI-001");
        var r2 = new AccountResponse("222", "Savings", new BigDecimal("2000"),
                new BigDecimal("2000"), true, "CLI-001");

        when(repository.findAll()).thenReturn(List.of(a1, a2));
        when(mapper.toResponse(a1)).thenReturn(r1);
        when(mapper.toResponse(a2)).thenReturn(r2);

        StepVerifier.create(accountUseCase.list().collectList())
                .assertNext(list -> assertThat(list).hasSize(2))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should deactivate account on delete")
    void shouldDeactivateOnDelete() {
        var account = createAccount("478758", new BigDecimal("2000"));

        when(repository.findByAccountNumber("478758")).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        StepVerifier.create(accountUseCase.delete("478758"))
                .verifyComplete();

        verify(repository).save(argThat(a -> !a.isActive()));
    }
}

