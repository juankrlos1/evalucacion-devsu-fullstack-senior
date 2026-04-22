package com.banking.account.infrastructure.persistence.mapper;

import com.banking.account.domain.model.Movement;
import com.banking.account.domain.model.MovementData;
import com.banking.account.infrastructure.persistence.entity.MovementEntity;
import org.springframework.stereotype.Component;

@Component
public class MovementEntityMapper {

    public Movement toDomain(MovementEntity entity) {
        var data = new MovementData(
                entity.getAccountNumber(),
                entity.getDate(),
                entity.getMovementType(),
                entity.getValue(),
                entity.getBalance()
        );
        return new Movement(entity.getMovementId(), data);
    }

    public MovementEntity toEntity(Movement domain) {
        var entity = new MovementEntity();
        entity.setMovementId(domain.getMovementId());
        entity.setAccountNumber(domain.getAccountNumber());
        entity.setDate(domain.getDate());
        entity.setMovementType(domain.getMovementType());
        entity.setValue(domain.getValue());
        entity.setBalance(domain.getBalance());
        return entity;
    }
}
