package com.banking.customer.infrastructure.web;

import com.banking.customer.application.dto.CustomerPatchRequest;
import com.banking.customer.application.dto.CustomerRequest;
import com.banking.customer.application.dto.CustomerResponse;
import com.banking.customer.application.port.in.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "CRUD de clientes (hereda de Persona)")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente con datos personales")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cliente creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validación fallida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "ID o identificación duplicada")
    })
    public Mono<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request)
                .map(r -> ApiResponse.success("Customer created successfully", r));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna todos los clientes registrados")
    public Mono<ApiResponse<List<CustomerResponse>>> list() {
        return customerService.list()
                .collectList()
                .map(r -> ApiResponse.success("Customers retrieved successfully", r));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Obtener cliente por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public Mono<ApiResponse<CustomerResponse>> get(
            @Parameter(description = "ID único del cliente", example = "CLI-001")
            @PathVariable String customerId) {
        return customerService.get(customerId)
                .map(r -> ApiResponse.success("Customer retrieved successfully", r));
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Reemplazar cliente", description = "Reemplaza todos los datos del cliente")
    public Mono<ApiResponse<CustomerResponse>> replace(
            @Parameter(description = "ID del cliente") @PathVariable String customerId,
            @Valid @RequestBody CustomerRequest request) {
        return customerService.replace(customerId, request)
                .map(r -> ApiResponse.success("Customer updated successfully", r));
    }

    @PatchMapping("/{customerId}")
    @Operation(summary = "Actualización parcial", description = "Actualiza solo los campos enviados")
    public Mono<ApiResponse<CustomerResponse>> patch(
            @Parameter(description = "ID del cliente") @PathVariable String customerId,
            @Valid @RequestBody CustomerPatchRequest request) {
        return customerService.patch(customerId, request)
                .map(r -> ApiResponse.success("Customer updated successfully", r));
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Desactivar cliente", description = "Soft delete — marca estado=false")
    public Mono<ApiResponse<Void>> delete(
            @Parameter(description = "ID del cliente") @PathVariable String customerId) {
        return customerService.delete(customerId)
                .then(Mono.fromCallable(() -> ApiResponse.<Void>success("Customer deleted successfully", null)));
    }
}
