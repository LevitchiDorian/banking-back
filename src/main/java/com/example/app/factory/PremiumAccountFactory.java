package com.example.app.factory;

import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.model.Account;
import com.example.app.model.AccountType;
import com.example.app.model.PremiumCheckingAccount;
import com.example.app.model.PremiumSavingsAccount;
import com.example.app.repository.AccountTypeRepository; // Import repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
// No longer injects overdraft limit via @Value

@Component("premiumAccountFactory")
public class PremiumAccountFactory implements AccountAbstractFactory {

    private final AccountTypeRepository accountTypeRepository;

    @Autowired
    public PremiumAccountFactory(AccountTypeRepository accountTypeRepository) {
        this.accountTypeRepository = accountTypeRepository;
    }

    @Override
    public Account createCheckingAccount(String accountNumber) {
        AccountType type = accountTypeRepository.findByTypeName("PREMIUM_CHECKING")
                .orElseThrow(() -> new RuntimeException("PREMIUM_CHECKING account type not found in database"));
        return new PremiumCheckingAccount(accountNumber, type.getOverdraftLimit());
    }

    @Override
    public Account createSavingsAccount(String accountNumber) {
        // PremiumSavingsAccount constructor does not take interest rate, it uses a static final one.
        // However, it's good practice to ensure the type exists.
        accountTypeRepository.findByTypeName("PREMIUM_SAVINGS")
                .orElseThrow(() -> new RuntimeException("PREMIUM_SAVINGS account type not found in database"));
        return new PremiumSavingsAccount(accountNumber);
    }
}