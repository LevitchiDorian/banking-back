package com.example.app.bridge;

import com.example.app.model.Account;
import java.math.BigDecimal;

// Implementarea concretă pentru Bridge
public interface NotificationSender {
    void send(String message, Account from, Account to, BigDecimal amount);
}