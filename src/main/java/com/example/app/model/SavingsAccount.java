package com.example.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.app.exception.InsufficientFundsException;
import com.example.app.interfaces.InterestBearing;

public abstract class SavingsAccount extends Account implements InterestBearing {
    public SavingsAccount(String accountNumber) {
        super(accountNumber);
    }

    // Metode comune pentru toate conturile Savings
    @Override
    public void deposit(BigDecimal amount) {
        super.setBalance(getBalance().add(amount));
        addTransaction(new Transaction(LocalDateTime.now(), amount, "Deposit"));
    }

    // Metode abstracte ce vor fi implementate în subclase
    @Override
    public abstract void applyInterest();
    @Override
    public abstract void withdraw(BigDecimal amount);
}
