package com.example.app.factory;

import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.model.Account;
import com.example.app.model.AccountType;
import com.example.app.model.StandardCheckingAccount;
import com.example.app.model.StandardSavingsAccount;
import com.example.app.repository.AccountTypeRepository; // Import repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
// No longer injects overdraft limit via @Value

@Component("standardAccountFactory")
public class StandardAccountFactory implements AccountAbstractFactory {

    private final AccountTypeRepository accountTypeRepository;

    @Autowired
    public StandardAccountFactory(AccountTypeRepository accountTypeRepository) {
        this.accountTypeRepository = accountTypeRepository;
    }

    @Override
    public Account createCheckingAccount(String accountNumber) {
        AccountType type = accountTypeRepository.findByTypeName("STANDARD_CHECKING")
                .orElseThrow(() -> new RuntimeException("STANDARD_CHECKING account type not found in database"));
        return new StandardCheckingAccount(accountNumber, type.getOverdraftLimit());
    }

    @Override
    public Account createSavingsAccount(String accountNumber) {
        AccountType type = accountTypeRepository.findByTypeName("STANDARD_SAVINGS")
                .orElseThrow(() -> new RuntimeException("STANDARD_SAVINGS account type not found in database"));
        return new StandardSavingsAccount(accountNumber, type.getInterestRate());
    }
}