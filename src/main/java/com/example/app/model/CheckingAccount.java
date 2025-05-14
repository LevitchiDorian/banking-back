package com.example.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.app.exception.InsufficientFundsException;

public abstract class CheckingAccount extends Account {
    protected BigDecimal overdraftLimit;

    public CheckingAccount(String accountNumber, BigDecimal overdraftLimit) {
        super(accountNumber);
        this.overdraftLimit = overdraftLimit;
    }

    // Metode comune pentru toate conturile Checking
    @Override
    public void deposit(BigDecimal amount) {
        super.setBalance(getBalance().add(amount));
        addTransaction(new Transaction(LocalDateTime.now(), amount, "Deposit"));
    }

    // Metoda withdraw va fi implementată în subclase
    @Override
    public abstract void withdraw(BigDecimal amount);

    // Implementare implicită pentru conturile de checking
    @Override
    public void applyInterest() {
        // Conturile de checking nu au dobândă
        // Putem lăsa implementarea goală sau arunca o excepție
        // throw new UnsupportedOperationException("Checking accounts don't support interest");
    }
}
