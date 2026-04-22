package com.banking.account.application.port.in;

import com.banking.account.application.dto.AccountStatementResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReportService {

    Flux<AccountStatementResponse> getAccountStatement(String startDate, String endDate, String customerId);

    Mono<String> getAccountStatementPdf(String startDate, String endDate, String customerId);
}
