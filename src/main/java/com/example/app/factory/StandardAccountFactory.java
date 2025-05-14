package com.example.app.factory;

import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.model.Account;
import com.example.app.model.StandardCheckingAccount;
import com.example.app.model.StandardSavingsAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("standardAccountFactory")
public class StandardAccountFactory implements AccountAbstractFactory {
    private final BigDecimal overdraftLimit;

    public  StandardAccountFactory(
            @Value("${overdraft.standard.limit}") String standardLimit
    ) {
        if(standardLimit == null || standardLimit.isBlank()) {
            throw new IllegalArgumentException("overdraft.standard.limit property is missing");
        }
        this.overdraftLimit = new BigDecimal(standardLimit);
    }

    @Override
    public Account createCheckingAccount(String accountNumber) {
        return new StandardCheckingAccount(accountNumber, overdraftLimit);
    }

    @Override
    public Account createSavingsAccount(String accountNumber) {
        return new StandardSavingsAccount(accountNumber, new BigDecimal("0.02"));
    }
}