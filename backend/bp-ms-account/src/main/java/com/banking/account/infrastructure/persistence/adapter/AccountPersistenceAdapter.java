package com.banking.account.infrastructure.persistence.adapter;

import com.banking.account.domain.model.Account;
import com.banking.account.domain.port.out.AccountRepository;
import com.banking.account.infrastructure.persistence.mapper.AccountEntityMapper;
import com.banking.account.infrastructure.persistence.repository.SpringDataAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountRepository {

    private final SpringDataAccountRepository jpaRepository;
    private final AccountEntityMapper mapper;

    @Override
    public Account save(Account account) {
        var entity = mapper.toEntity(account);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return jpaRepository.findById(accountNumber).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Account> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return jpaRepository.existsById(accountNumber);
    }
}
