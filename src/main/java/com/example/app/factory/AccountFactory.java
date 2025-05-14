package com.example.app.factory;

import com.example.app.model.Account;

public abstract class AccountFactory {
    public abstract Account createAccount(String accountNumber);
}