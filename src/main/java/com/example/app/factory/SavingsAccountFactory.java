package com.example.app.factory;

import com.example.app.flyweight.AccountProperties;
import com.example.app.flyweight.AccountTypeFlyweight;
import com.example.app.model.StandardSavingsAccount;
import com.example.app.model.Account;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class SavingsAccountFactory extends AccountFactory {
    @Override
    public Account createAccount(String accountNumber) {
        AccountProperties props = AccountTypeFlyweight.getAccountType(
                "STANDARD_SAVINGS",
                BigDecimal.ZERO,
                new BigDecimal("0.02")
        );
        return new StandardSavingsAccount(accountNumber, props.getInterestRate());
    }
}