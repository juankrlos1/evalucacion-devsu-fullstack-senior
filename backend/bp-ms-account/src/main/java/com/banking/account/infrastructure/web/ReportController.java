package com.banking.account.infrastructure.web;

import com.banking.account.application.dto.AccountStatementResponse;
import com.banking.account.application.port.in.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Estado de cuenta por rango de fechas y cliente")
public class ReportController {

    private static final String ADMIN_ROLE = "ADMIN";

    private final ReportService reportService;

    @GetMapping
    @Operation(summary = "Generar estado de cuenta",
            description = "Retorna movimientos del cliente en el rango de fechas. "
                    + "format=json (default) retorna JSON, format=pdf retorna PDF en base64.")
    public Mono<ApiResponse<?>> getReport(
            @Parameter(description = "Fecha inicio yyyy-MM-dd", example = "2022-02-01")
            @RequestParam String fechaInicio,
            @Parameter(description = "Fecha fin yyyy-MM-dd", example = "2022-02-28")
            @RequestParam String fechaFin,
            @Parameter(description = "ID del cliente", example = "CLI-001")
            @RequestParam String cliente,
            @Parameter(description = "Formato de salida: json o pdf", example = "json")
            @RequestParam(required = false, defaultValue = "json") String format,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        String effectiveCustomer = (userId != null && !ADMIN_ROLE.equals(userRole))
                ? userId : cliente;

        if ("pdf".equalsIgnoreCase(format)) {
            return reportService.getAccountStatementPdf(fechaInicio, fechaFin, effectiveCustomer)
                    .map(pdf -> ApiResponse.success("Report generated successfully",
                            Map.of("pdf", pdf)));
        }

        return reportService.getAccountStatement(fechaInicio, fechaFin, effectiveCustomer)
                .collectList()
                .map(rows -> ApiResponse.success("Report generated successfully", rows));
    }
}
