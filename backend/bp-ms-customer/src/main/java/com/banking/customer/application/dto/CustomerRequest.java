package com.banking.customer.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Datos para crear o reemplazar un cliente")
public record CustomerRequest(
        @NotBlank @Schema(example = "Jose Lema") String name,
        @Schema(example = "Male") String gender,
        @PositiveOrZero @Schema(example = "32") Integer age,
        @NotBlank @Schema(example = "0102030405") String identification,
        @NotBlank @Schema(example = "Otavalo sn y principal") String address,
        @NotBlank @Schema(example = "098254785") String phone,
        @NotBlank @Schema(example = "1234") String password,
        @NotNull @Schema(example = "true") Boolean active
) {}
