package com.banking.account.domain.model;

import com.banking.account.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovementTest {

    @Test
    @DisplayName("Should auto-determine Withdrawal type for negative value")
    void shouldDetermineWithdrawalType() {
        var data = new MovementData("478758", LocalDateTime.now(), null,
                new BigDecimal("-575"), new BigDecimal("1425"));

        assertThat(data.movementType()).isEqualTo("Withdrawal");
    }

    @Test
    @DisplayName("Should auto-determine Deposit type for positive value")
    void shouldDetermineDepositType() {
        var data = new MovementData("478758", LocalDateTime.now(), null,
                new BigDecimal("600"), new BigDecimal("2600"));

        assertThat(data.movementType()).isEqualTo("Deposit");
    }

    @Test
    @DisplayName("Should reject zero value")
    void shouldRejectZeroValue() {
        assertThatThrownBy(() -> new MovementData("478758", LocalDateTime.now(),
                null, BigDecimal.ZERO, new BigDecimal("1000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("zero");
    }

    @Test
    @DisplayName("Should reject negative balance")
    void shouldRejectNegativeBalance() {
        assertThatThrownBy(() -> new MovementData("478758", LocalDateTime.now(),
                null, new BigDecimal("-100"), new BigDecimal("-50")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    @DisplayName("Should reject blank account number")
    void shouldRejectBlankAccountNumber() {
        assertThatThrownBy(() -> new MovementData("", LocalDateTime.now(),
                null, new BigDecimal("100"), new BigDecimal("1100")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Account number");
    }

    @Test
    @DisplayName("Movement update returns new immutable instance")
    void shouldUpdateMovement() {
        var data = new MovementData("478758", LocalDateTime.now(), null,
                new BigDecimal("500"), new BigDecimal("2500"));
        var movement = new Movement("mv-1", data);

        var updated = movement.update(LocalDateTime.now(), new BigDecimal("300"), new BigDecimal("2300"));

        assertThat(updated.getMovementId()).isEqualTo("mv-1");
        assertThat(updated.getValue()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(updated.getBalance()).isEqualByComparingTo(new BigDecimal("2300"));
        assertThat(movement.getValue()).isEqualByComparingTo(new BigDecimal("500"));
    }
}

