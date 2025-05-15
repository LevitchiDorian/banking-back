package com.example.app.factory;

import com.example.app.model.Account;
import com.example.app.model.AccountType; // Import AccountType
import com.example.app.model.StandardCheckingAccount;
import org.springframework.stereotype.Component;
// No longer needs @Value or specific overdraft limit in constructor

@Component // Retain as component if still managed by Spring, or remove if purely utility
public class CheckingAccountFactory extends AccountFactory {

    // Constructor can be empty or removed if no dependencies
    public CheckingAccountFactory() {
    }

    @Override
    public Account createAccount(String accountNumber) {
        // This method is problematic as it doesn't have AccountType info.
        // It should ideally take AccountType or its properties.
        // For now, let's assume a default or throw an error.
        // The AccountService will use a more specific method.
        throw new UnsupportedOperationException("Use createAccount(String accountNumber, AccountType accountType) instead.");
    }

    // New method to be used by AccountService
    public Account createAccount(String accountNumber, AccountType accountType) {
        if (!accountType.getTypeName().toUpperCase().contains("CHECKING")) {
            throw new IllegalArgumentException("AccountType is not for a checking account: " + accountType.getTypeName());
        }
        // The specific type of checking account (Standard, Premium) will be decided by AccountService
        // or this factory can be split into StandardCheckingAccountFactory and PremiumCheckingAccountFactory.
        // For simplicity, let's assume StandardCheckingAccount if it's just "CheckingAccountFactory".
        // AccountService will use the more specific factories (StandardAccountFactory, PremiumAccountFactory).
        return new StandardCheckingAccount(accountNumber, accountType.getOverdraftLimit());
    }
}