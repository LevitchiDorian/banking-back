package com.example.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.app.exception.InsufficientFundsException;

public class StandardSavingsAccount extends SavingsAccount {
    private final BigDecimal interestRate;

    // Constructor modificat să primească interestRate
    public StandardSavingsAccount(String accountNumber, BigDecimal interestRate) {
        super(accountNumber);
        this.interestRate = interestRate;
    }

    @Override
    public void applyInterest() {
        BigDecimal interest = getBalance().multiply(interestRate);
        deposit(interest);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        if (getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in savings account");
        }
        super.setBalance(getBalance().subtract(amount));
        addTransaction(new Transaction(LocalDateTime.now(), amount.negate(), "Withdrawal"));
    }

    @Override
    public Account clone(String newAccountNumber) {
        StandardSavingsAccount clone = new StandardSavingsAccount(newAccountNumber, this.interestRate);
        clone.setBalance(this.getBalance());
        return clone;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }
}