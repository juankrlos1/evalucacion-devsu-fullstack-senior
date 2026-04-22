package com.banking.account.domain.port.out;

import com.banking.account.domain.model.Movement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovementRepository {

    Movement save(Movement movement);

    Optional<Movement> findByMovementId(String movementId);

    List<Movement> findAll();

    List<Movement> findByAccountNumbers(List<String> accountNumbers);

    void deleteByMovementId(String movementId);

    List<Movement> findByAccountNumbersAndDateRange(List<String> accountNumbers,
                                                     LocalDateTime start,
                                                     LocalDateTime end);

    /**
     * Returns the sum of all withdrawal (negative) values for a given account on a specific date.
     * Returns a positive BigDecimal representing the total withdrawn amount.
     */
    BigDecimal sumDailyWithdrawals(String accountNumber, LocalDateTime dayStart, LocalDateTime dayEnd);
}
