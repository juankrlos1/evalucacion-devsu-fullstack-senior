package com.banking.account.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "initial_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "customer_id", nullable = false)
    private String customerId;
}
