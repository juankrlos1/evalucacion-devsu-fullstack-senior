package com.banking.account.domain.model;

import com.banking.account.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void shouldApplyDepositMovement() {
        var data = new AccountData("Savings", new BigDecimal("1000"), new BigDecimal("1000"), true, "CLI-001");
        var account = new Account("478758", data);

        Account updated = account.applyMovement(new BigDecimal("500"));

        assertThat(updated.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    void shouldApplyWithdrawalMovement() {
        var data = new AccountData("Savings", new BigDecimal("2000"), new BigDecimal("2000"), true, "CLI-001");
        var account = new Account("478758", data);

        Account updated = account.applyMovement(new BigDecimal("-575"));

        assertThat(updated.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("1425"));
    }

    @Test
    void shouldRejectInsufficientBalance() {
        var data = new AccountData("Savings", new BigDecimal("100"), new BigDecimal("100"), true, "CLI-001");
        var account = new Account("478758", data);

        assertThatThrownBy(() -> account.applyMovement(new BigDecimal("-200")))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Saldo no disponible");
    }

    @Test
    void shouldAdjustMovement() {
        var data = new AccountData("Savings", new BigDecimal("1000"), new BigDecimal("500"), true, "CLI-001");
        var account = new Account("478758", data);

        Account adjusted = account.adjustMovement(new BigDecimal("-200"), new BigDecimal("-300"));

        assertThat(adjusted.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("400"));
    }
}
