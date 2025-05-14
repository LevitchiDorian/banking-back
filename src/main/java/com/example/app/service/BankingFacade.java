package com.example.app.service;

import com.example.app.model.Account;
import com.example.app.model.BankCustomer;
import com.example.app.model.Portfolio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BankingFacade {
    private final AccountService accountService;

    public BankingFacade(AccountService accountService) {
        this.accountService = accountService;
    }

    public BankCustomer createFullCustomer(String name, String email, String phone, String checkingAcc, String savingsAcc) {
        BankCustomer customer = new BankCustomer.Builder()
                .name(name)
                .email(email)
                .phone(phone)
                .build();

        accountService.createCheckingAccount(checkingAcc);
        accountService.createSavingsAccount(savingsAcc);

        return customer;
    }

    public void transferBetweenPortfolios(Portfolio source, Portfolio destination, BigDecimal amount) {
        source.getAccounts().forEach(srcAccount -> {
            destination.getAccounts().forEach(destAccount -> {
                accountService.transfer((Account) srcAccount, (Account) destAccount, amount);
            });
        });
    }
}