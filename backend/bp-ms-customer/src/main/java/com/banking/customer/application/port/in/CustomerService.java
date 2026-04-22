package com.banking.customer.application.port.in;

import com.banking.customer.application.dto.CustomerPatchRequest;
import com.banking.customer.application.dto.CustomerRequest;
import com.banking.customer.application.dto.CustomerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Input port defining customer operations.
 */
public interface CustomerService {

    Mono<CustomerResponse> create(CustomerRequest request);

    Mono<CustomerResponse> get(String customerId);

    Flux<CustomerResponse> list();

    Mono<CustomerResponse> replace(String customerId, CustomerRequest request);

    Mono<CustomerResponse> patch(String customerId, CustomerPatchRequest request);

    Mono<Void> delete(String customerId);
}
