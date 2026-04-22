package com.banking.account.application.mapper;

import com.banking.account.application.dto.AccountRequest;
import com.banking.account.application.dto.AccountResponse;
import com.banking.account.domain.model.Account;
import com.banking.account.domain.model.AccountData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    default Account toDomain(String accountNumber, AccountRequest req) {
        var data = new AccountData(
                req.accountType(), req.initialBalance(), req.initialBalance(),
                req.active(), req.customerId()
        );
        return new Account(accountNumber, data);
    }

    default AccountResponse toResponse(Account a) {
        return new AccountResponse(
                a.getAccountNumber(), a.getAccountType(),
                a.getInitialBalance(), a.getAvailableBalance(),
                a.isActive(), a.getCustomerId()
        );
    }
}
