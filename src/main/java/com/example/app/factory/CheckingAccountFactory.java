package com.example.app.factory;

import com.example.app.flyweight.AccountProperties;
import com.example.app.flyweight.AccountTypeFlyweight;
import com.example.app.model.StandardCheckingAccount;
import com.example.app.model.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class CheckingAccountFactory extends AccountFactory {

    private final BigDecimal overdraftLimit;

    public CheckingAccountFactory(
            @Value("${overdraft.standard.limit}") String standardLimit
    ) {
        if(standardLimit == null || standardLimit.isBlank()) {
            throw new IllegalArgumentException("overdraft.standard.limit property is missing");
        }
        this.overdraftLimit = new BigDecimal(standardLimit);
    }

    @Override
    public Account createAccount(String accountNumber) {
        // Folosește Flyweight pentru proprietăți
        AccountProperties props = AccountTypeFlyweight.getAccountType(
                "STANDARD_CHECKING",
                overdraftLimit,
                BigDecimal.ZERO // Fără dobândă pentru checking
        );
        return new StandardCheckingAccount(accountNumber, props.getOverdraftLimit());
    }
}