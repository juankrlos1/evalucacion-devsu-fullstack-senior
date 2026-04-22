package com.banking.account.infrastructure.persistence.adapter;

import com.banking.account.domain.model.Movement;
import com.banking.account.domain.port.out.MovementRepository;
import com.banking.account.infrastructure.persistence.mapper.MovementEntityMapper;
import com.banking.account.infrastructure.persistence.repository.SpringDataMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MovementPersistenceAdapter implements MovementRepository {

    private final SpringDataMovementRepository jpaRepository;
    private final MovementEntityMapper mapper;

    @Override
    public Movement save(Movement movement) {
        var entity = mapper.toEntity(movement);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Movement> findByMovementId(String movementId) {
        return jpaRepository.findById(movementId).map(mapper::toDomain);
    }

    @Override
    public List<Movement> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Movement> findByAccountNumbers(List<String> accountNumbers) {
        if (accountNumbers.isEmpty()) return List.of();
        return jpaRepository.findByAccountNumberIn(accountNumbers).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteByMovementId(String movementId) {
        jpaRepository.deleteById(movementId);
    }

    @Override
    public List<Movement> findByAccountNumbersAndDateRange(
            List<String> accountNumbers, LocalDateTime start, LocalDateTime end) {
        return jpaRepository
                .findByAccountNumberInAndDateBetweenOrderByDateAsc(accountNumbers, start, end)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public BigDecimal sumDailyWithdrawals(String accountNumber, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return jpaRepository.sumDailyWithdrawals(accountNumber, dayStart, dayEnd);
    }
}
