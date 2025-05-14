package com.example.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.app.exception.InsufficientFundsException;

public class StandardCheckingAccount extends CheckingAccount {
    public StandardCheckingAccount(String accountNumber, BigDecimal overdraftLimit) {
        super(accountNumber, overdraftLimit);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        if (getBalance().subtract(amount).compareTo(overdraftLimit.negate()) < 0) {
            throw new InsufficientFundsException("Exceeds overdraft limit");
        }
        super.setBalance(getBalance().subtract(amount));
        addTransaction(new Transaction(LocalDateTime.now(), amount.negate(), "Withdrawal"));
    }

    @Override
    public Account clone(String newAccountNumber) {
        StandardCheckingAccount clone = new StandardCheckingAccount(newAccountNumber, this.overdraftLimit);
        clone.setBalance(this.getBalance());
        return clone;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
}