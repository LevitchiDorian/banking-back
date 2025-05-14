package com.example.app.service;

import com.example.app.decorator.InsuranceDecorator;
import com.example.app.decorator.PremiumBenefitsDecorator;
import com.example.app.factory.AccountFactory;
import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.interfaces.NotificationService;
import com.example.app.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service ("accountServiceImpl") // Adăugat qualifier
public class AccountService {
    private final NotificationService notificationService;
    private final AccountAbstractFactory accountAbstractFactory;
    private final AccountFactory checkingAccountFactory;
    private final AccountFactory savingsAccountFactory;
    private final Map<String, Account> accounts = new HashMap<>();

    @Autowired
    public AccountService(
            @Qualifier("emailNotificationService") NotificationService notificationService,
            @Qualifier("premiumAccountFactory") AccountAbstractFactory accountAbstractFactory,
            @Qualifier("checkingAccountFactory") AccountFactory checkingAccountFactory,
            @Qualifier("savingsAccountFactory") AccountFactory savingsAccountFactory

    ) {
        this.notificationService = notificationService;
        this.accountAbstractFactory = accountAbstractFactory;
        this.checkingAccountFactory = checkingAccountFactory;
        this.savingsAccountFactory = savingsAccountFactory;
    }

    // Factory Methods
    public Account createPremiumCheckingAccount(String accountNumber) {
        Account account = accountAbstractFactory.createCheckingAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createPremiumSavingsAccount(String accountNumber) {
        Account account = accountAbstractFactory.createSavingsAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createStandardCheckingAccount(String accountNumber) {
        Account account = checkingAccountFactory.createAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createStandardSavingsAccount(String accountNumber) {
        Account account = savingsAccountFactory.createAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    // Account Operations
    public void transfer(Account from, Account to, BigDecimal amount) {
        from.withdraw(amount);
        to.deposit(amount);
        notificationService.sendTransferNotification(from, to, amount);
    }

    public Account getAccountByNumber(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        return account;
    }

    public Account createCheckingAccount(String accountNumber) {
        return createStandardCheckingAccount(accountNumber);
    }

    public Account createSavingsAccount(String accountNumber) {
        return createStandardSavingsAccount(accountNumber);
    }

    public Account cloneAccount(String originalNumber, String newNumber) {
        Account original = accounts.get(originalNumber);
        if (original == null) throw new IllegalArgumentException("Account not found");
        Account clone = original.clone(newNumber);
        accounts.put(newNumber, clone);
        return clone;
    }

    // Decorator Methods
    public Account addInsurance(Account account, BigDecimal benefit) {
        Account decorated = new InsuranceDecorator(account, benefit);
        accounts.put(account.getAccountNumber(), decorated);
        return decorated;
    }

    public Account addPremiumBenefits(Account account) {
        Account decorated = new PremiumBenefitsDecorator(account);
        accounts.put(account.getAccountNumber(), decorated);
        return decorated;
    }

    public Map<String, Account> getAllAccounts() {
        return Collections.unmodifiableMap(accounts);
    }
}