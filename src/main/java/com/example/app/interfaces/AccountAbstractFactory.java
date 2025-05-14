package com.example.app.interfaces;
import com.example.app.model.Account;

public interface AccountAbstractFactory {
    Account createCheckingAccount(String accountNumber);
    Account createSavingsAccount(String accountNumber);
}
