package com.banking.account.infrastructure.web;

import com.banking.account.application.dto.AccountPatchRequest;
import com.banking.account.application.dto.AccountRequest;
import com.banking.account.application.dto.AccountResponse;
import com.banking.account.application.port.in.AccountService;
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
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "CRUD de cuentas bancarias")
public class AccountController {

    private static final String ADMIN_ROLE = "ADMIN";

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cuenta", description = "Registra una nueva cuenta vinculada a un cliente")
    public Mono<ApiResponse<AccountResponse>> create(@Valid @RequestBody AccountRequest request) {
        return accountService.create(request)
                .map(r -> ApiResponse.success("Account created successfully", r));
    }

    @GetMapping
    @Operation(summary = "Listar cuentas")
    public Mono<ApiResponse<List<AccountResponse>>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        var source = (userId != null && !ADMIN_ROLE.equals(userRole))
                ? accountService.listByCustomerId(userId)
                : accountService.list();
        return source.collectList()
                .map(r -> ApiResponse.success("Accounts retrieved successfully", r));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Obtener cuenta por número")
    public Mono<ApiResponse<AccountResponse>> get(
            @Parameter(description = "Número de cuenta", example = "478758")
            @PathVariable String accountNumber) {
        return accountService.get(accountNumber)
                .map(r -> ApiResponse.success("Account retrieved successfully", r));
    }

    @PutMapping("/{accountNumber}")
    @Operation(summary = "Reemplazar cuenta")
    public Mono<ApiResponse<AccountResponse>> replace(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountRequest request) {
        return accountService.replace(accountNumber, request)
                .map(r -> ApiResponse.success("Account updated successfully", r));
    }

    @PatchMapping("/{accountNumber}")
    @Operation(summary = "Actualización parcial de cuenta")
    public Mono<ApiResponse<AccountResponse>> patch(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountPatchRequest request) {
        return accountService.patch(accountNumber, request)
                .map(r -> ApiResponse.success("Account updated successfully", r));
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Desactivar cuenta", description = "Soft delete — marca estado=false")
    public Mono<ApiResponse<Void>> delete(@PathVariable String accountNumber) {
        return accountService.delete(accountNumber)
                .then(Mono.fromCallable(() -> ApiResponse.<Void>success("Account deleted successfully", null)));
    }
}
