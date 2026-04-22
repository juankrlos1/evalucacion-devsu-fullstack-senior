package com.banking.account.infrastructure.persistence.repository;

import com.banking.account.infrastructure.persistence.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataMovementRepository extends JpaRepository<MovementEntity, String> {

    List<MovementEntity> findByAccountNumberIn(List<String> accountNumbers);

    List<MovementEntity> findByAccountNumberInAndDateBetweenOrderByDateAsc(
            List<String> accountNumbers, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(ABS(m.value)), 0) FROM MovementEntity m " +
           "WHERE m.accountNumber = :accountNumber " +
           "AND m.value < 0 " +
           "AND m.date >= :dayStart " +
           "AND m.date < :dayEnd")
    BigDecimal sumDailyWithdrawals(
            @Param("accountNumber") String accountNumber,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);
}
