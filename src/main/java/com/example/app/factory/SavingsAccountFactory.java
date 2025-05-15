package com.example.app.factory;

import com.example.app.model.Account;
import com.example.app.model.AccountType; // Import AccountType
import com.example.app.model.StandardSavingsAccount;
import org.springframework.stereotype.Component;
// No longer needs hardcoded interest rate

@Component // Retain as component
public class SavingsAccountFactory extends AccountFactory {

    public SavingsAccountFactory() {
    }

    @Override
    public Account createAccount(String accountNumber) {
        throw new UnsupportedOperationException("Use createAccount(String accountNumber, AccountType accountType) instead.");
    }

    public Account createAccount(String accountNumber, AccountType accountType) {
        if (!accountType.getTypeName().toUpperCase().contains("SAVINGS")) {
            throw new IllegalArgumentException("AccountType is not for a savings account: " + accountType.getTypeName());
        }
        // Similar to CheckingAccountFactory, this is a generic factory.
        // AccountService will likely use StandardAccountFactory or PremiumAccountFactory.
        return new StandardSavingsAccount(accountNumber, accountType.getInterestRate());
    }
}