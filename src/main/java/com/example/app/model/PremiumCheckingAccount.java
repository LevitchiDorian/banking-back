package com.example.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.app.exception.InsufficientFundsException;

public class PremiumCheckingAccount extends CheckingAccount {
    public PremiumCheckingAccount(String accountNumber, BigDecimal overdraftLimit) {
        super(accountNumber, overdraftLimit);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        if (getBalance().subtract(amount).compareTo(overdraftLimit.negate().multiply(BigDecimal.valueOf(2))) < 0) {
            throw new InsufficientFundsException("Exceeds premium overdraft limit");
        }
        super.setBalance(getBalance().subtract(amount));
        addTransaction(new Transaction(LocalDateTime.now(), amount.negate(), "Premium Withdrawal"));
    }

    @Override
    public Account clone(String newAccountNumber) {
        PremiumCheckingAccount clone = new PremiumCheckingAccount(newAccountNumber, this.overdraftLimit);
        clone.setBalance(this.getBalance());
        return clone;
    }
}