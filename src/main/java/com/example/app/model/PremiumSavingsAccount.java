package com.example.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class PremiumSavingsAccount extends SavingsAccount {
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.05"); // Dobândă mai mare

    public PremiumSavingsAccount(String accountNumber) {
        super(accountNumber);
    }

    @Override
    public void applyInterest() {
        BigDecimal interest = getBalance().multiply(INTEREST_RATE);
        deposit(interest);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        // Permite retrageri peste sold cu comision
        BigDecimal fee = amount.multiply(BigDecimal.valueOf(0.01));
        super.setBalance(getBalance().subtract(amount.add(fee)));
        addTransaction(new Transaction(LocalDateTime.now(), amount.negate(), "Premium Withdrawal"));
    }

    @Override
    public Account clone(String newAccountNumber) {
        PremiumSavingsAccount clone = new PremiumSavingsAccount(newAccountNumber);
        clone.setBalance(this.getBalance());
        return clone;
    }

    public BigDecimal getInterestRate() {
        return INTEREST_RATE;
    }
}