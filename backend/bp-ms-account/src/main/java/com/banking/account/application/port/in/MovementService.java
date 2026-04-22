package com.banking.account.application.port.in;

import com.banking.account.application.dto.MovementPatchRequest;
import com.banking.account.application.dto.MovementRequest;
import com.banking.account.application.dto.MovementResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MovementService {

    Mono<MovementResponse> create(MovementRequest request);

    Mono<MovementResponse> get(String movementId);

    Flux<MovementResponse> list();

    Flux<MovementResponse> listByAccountNumbers(List<String> accountNumbers);

    Mono<MovementResponse> replace(String movementId, MovementRequest request);

    Mono<MovementResponse> patch(String movementId, MovementPatchRequest request);

    Mono<Void> delete(String movementId);
}
