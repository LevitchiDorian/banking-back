package com.example.app.decorator;

import com.example.app.model.Account;
import java.math.BigDecimal;

public class PremiumBenefitsDecorator extends AccountDecorator {
    public PremiumBenefitsDecorator(Account decoratedAccount) {
        super(decoratedAccount);
    }

    // Suprascriere deposit() cu bonus
    @Override
    public void deposit(BigDecimal amount) {
        BigDecimal bonus = amount.multiply(BigDecimal.valueOf(0.05));
        decoratedAccount.deposit(amount.add(bonus));
    }

    // Restul metodelor
    @Override
    public Account clone(String newAccountNumber) {
        return new PremiumBenefitsDecorator(
                decoratedAccount.clone(newAccountNumber)
        );
    }

    @Override
    public void withdraw(BigDecimal amount) {
        decoratedAccount.withdraw(amount);
    }
}