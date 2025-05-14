package com.example.app.controller;

import com.example.app.model.Account;
import com.example.app.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;


    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/create-checking")
    public String createCheckingAccount(@RequestParam String accountNumber) {
        Account account = accountService.createCheckingAccount(accountNumber);
        return "Cont checking creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }

    @GetMapping("/create-savings")
    public String createSavingsAccount(@RequestParam String accountNumber) {
        Account account = accountService.createSavingsAccount(accountNumber);
        return "Cont savings creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }

    @GetMapping("/create-premium-checking")
    public String createPremiumCheckingAccount(@RequestParam String accountNumber) {
        Account account = accountService.createPremiumCheckingAccount(accountNumber);
        return "Cont checking premium creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }

    @GetMapping("/create-premium-savings")
    public String createPremiumSavingsAccount(@RequestParam String accountNumber) {
        Account account = accountService.createPremiumSavingsAccount(accountNumber);
        return "Cont savings premium creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }
}