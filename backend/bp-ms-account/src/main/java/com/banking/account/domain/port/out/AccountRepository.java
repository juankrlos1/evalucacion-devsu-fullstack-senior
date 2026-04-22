package com.banking.account.domain.port.out;

import com.banking.account.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findAll();

    List<Account> findByCustomerId(String customerId);

    boolean existsByAccountNumber(String accountNumber);
}
