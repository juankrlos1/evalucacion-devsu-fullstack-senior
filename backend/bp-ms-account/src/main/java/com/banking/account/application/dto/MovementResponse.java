package com.banking.account.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovementResponse(
        String movementId,
        String accountNumber,
        LocalDateTime date,
        String movementType,
        BigDecimal value,
        BigDecimal balance
) {}
