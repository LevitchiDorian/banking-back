package com.example.app.model;

import com.example.app.interfaces.AccountComponent;
import com.example.app.interfaces.AccountOperations;
import com.example.app.interfaces.TransactionHistory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Account implements AccountOperations, TransactionHistory, AccountComponent {
    private String accountNumber;
    private BigDecimal balance;
    private final List<Transaction> transactions;

    public Account(String accountNumber) {
        this.accountNumber = accountNumber;
        this.balance = BigDecimal.ZERO;
        this.transactions = new ArrayList<>();
    }

    // Setter nou pentru accountNumber
    protected void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    protected void setBalance(BigDecimal balance) { this.balance = balance; }
    protected void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public void add(AccountComponent component) {
        throw new UnsupportedOperationException("Operație nesuportată pentru cont individual");
    }


    public abstract Account clone(String newAccountNumber); // Metodă abstractă

    public abstract void applyInterest();

    @Override
    public void remove(AccountComponent component) {
        throw new UnsupportedOperationException("Operație nesuportată pentru cont individual");
    }
}
