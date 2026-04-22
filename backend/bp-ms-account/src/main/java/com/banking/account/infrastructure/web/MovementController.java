package com.banking.account.infrastructure.web;

import com.banking.account.application.dto.MovementPatchRequest;
import com.banking.account.application.dto.MovementRequest;
import com.banking.account.application.dto.MovementResponse;
import com.banking.account.application.port.in.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos", description = "Gestión de movimientos bancarios (depósitos/retiros)")
public class MovementController {

    private static final String ADMIN_ROLE = "ADMIN";

    private final MovementService movementService;
    private final com.banking.account.application.port.in.AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear movimiento",
            description = "Valor positivo=depósito, negativo=retiro. Valida saldo y límite diario ($1000)")
    public Mono<ApiResponse<MovementResponse>> create(@Valid @RequestBody MovementRequest request) {
        return movementService.create(request)
                .map(r -> ApiResponse.success("Movement created successfully", r));
    }

    @GetMapping
    @Operation(summary = "Listar movimientos")
    public Mono<ApiResponse<List<MovementResponse>>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        if (userId != null && !ADMIN_ROLE.equals(userRole)) {
            return accountService.listByCustomerId(userId)
                    .map(a -> a.accountNumber())
                    .collectList()
                    .flatMap(accountNumbers -> movementService.listByAccountNumbers(accountNumbers)
                            .collectList())
                    .map(r -> ApiResponse.success("Movements retrieved successfully", r));
        }
        return movementService.list()
                .collectList()
                .map(r -> ApiResponse.success("Movements retrieved successfully", r));
    }

    @GetMapping("/{movementId}")
    @Operation(summary = "Obtener movimiento por ID")
    public Mono<ApiResponse<MovementResponse>> get(
            @Parameter(description = "UUID del movimiento") @PathVariable String movementId) {
        return movementService.get(movementId)
                .map(r -> ApiResponse.success("Movement retrieved successfully", r));
    }

    @PutMapping("/{movementId}")
    @Operation(summary = "Reemplazar movimiento", description = "Ajusta el saldo de la cuenta automáticamente")
    public Mono<ApiResponse<MovementResponse>> replace(
            @PathVariable String movementId,
            @Valid @RequestBody MovementRequest request) {
        return movementService.replace(movementId, request)
                .map(r -> ApiResponse.success("Movement updated successfully", r));
    }

    @PatchMapping("/{movementId}")
    @Operation(summary = "Actualización parcial de movimiento")
    public Mono<ApiResponse<MovementResponse>> patch(
            @PathVariable String movementId,
            @Valid @RequestBody MovementPatchRequest request) {
        return movementService.patch(movementId, request)
                .map(r -> ApiResponse.success("Movement updated successfully", r));
    }

    @DeleteMapping("/{movementId}")
    @Operation(summary = "Eliminar movimiento", description = "Revierte el saldo y elimina el registro")
    public Mono<ApiResponse<Void>> delete(@PathVariable String movementId) {
        return movementService.delete(movementId)
                .then(Mono.fromCallable(() -> ApiResponse.<Void>success("Movement deleted successfully", null)));
    }
}
