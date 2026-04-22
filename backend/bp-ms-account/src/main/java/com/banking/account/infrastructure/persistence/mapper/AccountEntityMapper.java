package com.banking.account.infrastructure.persistence.mapper;

import com.banking.account.domain.model.Account;
import com.banking.account.domain.model.AccountData;
import com.banking.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public Account toDomain(AccountEntity entity) {
        var data = new AccountData(
                entity.getAccountType(),
                entity.getInitialBalance(),
                entity.getAvailableBalance(),
                entity.isActive(),
                entity.getCustomerId()
        );
        return new Account(entity.getAccountNumber(), data);
    }

    public AccountEntity toEntity(Account domain) {
        var entity = new AccountEntity();
        entity.setAccountNumber(domain.getAccountNumber());
        entity.setAccountType(domain.getAccountType());
        entity.setInitialBalance(domain.getInitialBalance());
        entity.setAvailableBalance(domain.getAvailableBalance());
        entity.setActive(domain.isActive());
        entity.setCustomerId(domain.getCustomerId());
        return entity;
    }
}
