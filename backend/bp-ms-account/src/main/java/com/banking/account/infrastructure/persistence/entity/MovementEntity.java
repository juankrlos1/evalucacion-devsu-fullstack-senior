package com.banking.account.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movements")
public class MovementEntity {

    @Id
    @Column(name = "movement_id")
    private String movementId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "movement_type", nullable = false)
    private String movementType;

    @Column(nullable = false, precision = 19, scale = 2, name = "movement_value")
    private BigDecimal value;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
}
