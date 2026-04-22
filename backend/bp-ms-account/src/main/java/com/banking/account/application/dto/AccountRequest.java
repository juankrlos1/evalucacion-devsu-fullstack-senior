package com.banking.account.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o reemplazar una cuenta")
public record AccountRequest(
        @NotBlank @Schema(example = "Savings") String accountType,
        @NotNull @PositiveOrZero @Schema(example = "2000.00") BigDecimal initialBalance,
        @NotNull @Schema(example = "true") Boolean active,
        @NotBlank @Schema(example = "CLI-001") String customerId
) {}
