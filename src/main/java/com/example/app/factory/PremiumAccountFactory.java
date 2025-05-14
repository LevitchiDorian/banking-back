package com.example.app.factory;

import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.model.Account;
import com.example.app.model.PremiumCheckingAccount;
import com.example.app.model.PremiumSavingsAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("premiumAccountFactory")
public class PremiumAccountFactory implements AccountAbstractFactory {
    private final BigDecimal overdraftLimit;

    public PremiumAccountFactory(
            @Value("${overdraft.premium.limit}") String premiumLimit
    ) {
        if(premiumLimit == null || premiumLimit.isBlank()) {
            throw new IllegalArgumentException("overdraft.standard.limit property is missing");
        }
        this.overdraftLimit = new BigDecimal(premiumLimit);
    }
    @Override
    public Account createCheckingAccount(String accountNumber) {
        return new PremiumCheckingAccount(accountNumber, overdraftLimit);
    }

    @Override
    public Account createSavingsAccount(String accountNumber) {
        return new PremiumSavingsAccount(accountNumber);
    }
}