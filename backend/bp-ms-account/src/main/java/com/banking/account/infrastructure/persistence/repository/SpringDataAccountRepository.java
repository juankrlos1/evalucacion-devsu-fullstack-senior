package com.banking.account.infrastructure.persistence.repository;

import com.banking.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, String> {

    List<AccountEntity> findByCustomerId(String customerId);
}
