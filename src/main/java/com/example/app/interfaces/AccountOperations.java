package com.example.app.interfaces;

import com.example.app.model.Account;

import java.math.BigDecimal;

public interface AccountOperations {
    void deposit(BigDecimal amount);
    void withdraw(BigDecimal amount);

}
