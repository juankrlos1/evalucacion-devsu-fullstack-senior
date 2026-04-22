package com.banking.account.application.usecase;

import com.banking.account.application.dto.AccountStatementResponse;
import com.banking.account.application.port.in.ReportService;
import com.banking.account.domain.model.Account;
import com.banking.account.domain.model.Movement;
import com.banking.account.domain.port.out.AccountRepository;
import com.banking.account.domain.port.out.MovementRepository;
import com.banking.account.infrastructure.report.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use case for generating account statements (JSON and PDF).
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportUseCase implements ReportService {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final PdfReportGenerator pdfReportGenerator;

    @Override
    @Transactional(readOnly = true)
    public Flux<AccountStatementResponse> getAccountStatement(String startDateStr, String endDateStr, String customerId) {
        return Mono.fromCallable(() -> buildStatementRows(startDateStr, endDateStr, customerId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<String> getAccountStatementPdf(String startDateStr, String endDateStr, String customerId) {
        return Mono.fromCallable(() -> {
            List<AccountStatementResponse> rows = buildStatementRows(startDateStr, endDateStr, customerId);
            return pdfReportGenerator.generate(rows, customerId, startDateStr + " to " + endDateStr);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<AccountStatementResponse> buildStatementRows(String startDateStr, String endDateStr, String customerId) {
        LocalDate startDate = LocalDate.parse(startDateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(endDateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        List<String> accountNumbers = accounts.stream().map(Account::getAccountNumber).toList();

        if (accountNumbers.isEmpty()) {
            log.info("No accounts found for customerId={}", customerId);
            return List.of();
        }

        Map<String, Account> accountMap = accounts.stream()
                .collect(Collectors.toMap(Account::getAccountNumber, Function.identity()));

        List<Movement> movements = movementRepository.findByAccountNumbersAndDateRange(accountNumbers, start, end);

        List<AccountStatementResponse> rows = new ArrayList<>();
        for (Movement m : movements) {
            Account account = accountMap.get(m.getAccountNumber());
            if (account == null) continue;

            rows.add(new AccountStatementResponse(
                    m.getDate().toLocalDate(),
                    customerId,
                    m.getAccountNumber(),
                    account.getAccountType(),
                    account.getInitialBalance(),
                    account.isActive(),
                    m.getValue(),
                    m.getBalance()
            ));
        }
        log.info("Statement built: customerId={}, range={}..{}, accounts={}, movements={}",
                customerId, startDateStr, endDateStr, accounts.size(), rows.size());
        return rows;
    }
}
