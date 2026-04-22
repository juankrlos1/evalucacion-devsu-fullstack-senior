package com.banking.account.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Datos para crear un movimiento. Positivo=depósito, negativo=retiro")
public record MovementRequest(
        @NotBlank @Schema(example = "478758") String accountNumber,
        @NotNull @Schema(example = "-575.00", description = "Positivo=depósito, negativo=retiro") BigDecimal value
) {}
