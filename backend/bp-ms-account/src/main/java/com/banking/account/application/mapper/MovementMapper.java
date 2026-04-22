package com.banking.account.application.mapper;

import com.banking.account.application.dto.MovementResponse;
import com.banking.account.domain.model.Movement;
import com.banking.account.domain.model.MovementData;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MovementMapper {

    default Movement toDomain(String accountNumber, BigDecimal value, BigDecimal balance) {
        var data = new MovementData(accountNumber, LocalDateTime.now(), null, value, balance);
        return new Movement(UUID.randomUUID().toString(), data);
    }

    default MovementResponse toResponse(Movement m) {
        return new MovementResponse(
                m.getMovementId(), m.getAccountNumber(), m.getDate(),
                m.getMovementType(), m.getValue(), m.getBalance()
        );
    }
}
