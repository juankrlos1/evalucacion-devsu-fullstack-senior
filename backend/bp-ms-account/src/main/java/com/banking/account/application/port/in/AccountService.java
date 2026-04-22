package com.banking.account.application.port.in;

import com.banking.account.application.dto.AccountPatchRequest;
import com.banking.account.application.dto.AccountRequest;
import com.banking.account.application.dto.AccountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Mono<AccountResponse> create(AccountRequest request);

    Mono<AccountResponse> get(String accountNumber);

    Flux<AccountResponse> list();

    Flux<AccountResponse> listByCustomerId(String customerId);

    Mono<AccountResponse> replace(String accountNumber, AccountRequest request);

    Mono<AccountResponse> patch(String accountNumber, AccountPatchRequest request);

    Mono<Void> delete(String accountNumber);
}
