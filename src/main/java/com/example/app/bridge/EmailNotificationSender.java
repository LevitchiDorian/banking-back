package com.example.app.bridge;

import com.example.app.model.Account;
import java.math.BigDecimal;

public class EmailNotificationSender implements NotificationSender {
    @Override
    public void send(String message, Account from, Account to, BigDecimal amount) {
        System.out.println("[EMAIL] " + message);
        System.out.println("From: " + from.getAccountNumber());
        System.out.println("To: " + to.getAccountNumber());
        System.out.println("Amount: " + amount + " LEI");
    }
}