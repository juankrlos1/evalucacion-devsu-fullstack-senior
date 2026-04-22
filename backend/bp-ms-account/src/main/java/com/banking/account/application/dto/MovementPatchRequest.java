package com.banking.account.application.dto;

import java.math.BigDecimal;

public record MovementPatchRequest(
        BigDecimal value
) {}
