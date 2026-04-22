package com.banking.account.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a financial movement/transaction on an account.
 */
@Getter
public class Movement {

    private final String movementId;
    private final MovementData data;

    public Movement(String movementId, MovementData data) {
        this.movementId = movementId;
        this.data = data;
    }

    public String getAccountNumber() { return data.accountNumber(); }
    public LocalDateTime getDate() { return data.date(); }
    public String getMovementType() { return data.movementType(); }
    public BigDecimal getValue() { return data.value(); }
    public BigDecimal getBalance() { return data.balance(); }

    /**
     * Updates movement data, returning a new immutable instance.
     */
    public Movement update(LocalDateTime date, BigDecimal value, BigDecimal balance) {
        var newData = new MovementData(data.accountNumber(), date, null, value, balance);
        return new Movement(this.movementId, newData);
    }
}
