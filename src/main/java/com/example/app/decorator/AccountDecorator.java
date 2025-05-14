package com.example.app.decorator;

import com.example.app.interfaces.InterestBearing;
import com.example.app.model.Account;
import com.example.app.model.SavingsAccount;
import com.example.app.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

public abstract class AccountDecorator extends Account implements InterestBearing {
    protected final Account decoratedAccount;

    public AccountDecorator(Account decoratedAccount) {
        super(decoratedAccount.getAccountNumber());
        this.decoratedAccount = decoratedAccount;
        super.setBalance(decoratedAccount.getBalance());
    }

    // Implementare delegată pentru InterestBearing
    @Override
    public void applyInterest() {
        if (decoratedAccount instanceof SavingsAccount) {
            ((SavingsAccount) decoratedAccount).applyInterest();
        }
    }

    // Restul metodelor rămân aceleași
    @Override
    public String getAccountNumber() {
        return decoratedAccount.getAccountNumber();
    }

    @Override
    public BigDecimal getBalance() {
        return decoratedAccount.getBalance();
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        return decoratedAccount.getTransactionHistory();
    }

    @Override
    public abstract Account clone(String newAccountNumber);
}