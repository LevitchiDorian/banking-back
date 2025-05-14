package com.example.app.service;

import com.example.app.exception.HighRiskTransactionException;
import com.example.app.factory.AccountFactory;
import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.interfaces.NotificationService;
import com.example.app.model.Account;
import com.example.app.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service("accountService") // Înlocuiește implementarea originală

public class AccountServiceSecurityProxy extends AccountService {
    private static final BigDecimal HIGH_RISK_LIMIT = new BigDecimal("10000");
    private final AccountService realService;

    @Autowired
    public AccountServiceSecurityProxy(
            @Qualifier("accountServiceImpl") AccountService realService,
            @Qualifier("emailNotificationService") NotificationService notificationService,
            @Qualifier("premiumAccountFactory") AccountAbstractFactory accountAbstractFactory,
            @Qualifier("checkingAccountFactory") AccountFactory checkingAccountFactory,
            @Qualifier("savingsAccountFactory") AccountFactory savingsAccountFactory
    ) {
        super(notificationService, accountAbstractFactory, checkingAccountFactory, savingsAccountFactory);
        this.realService = realService;
    }

    @Override
    public void transfer(Account from, Account to, BigDecimal amount) {
        if(amount.compareTo(HIGH_RISK_LIMIT) > 0) {
            throw new HighRiskTransactionException("Tranzacții peste 10,000 LEI necesită aprobare manager!");
        }
        super.transfer(from, to, amount);
    }
}