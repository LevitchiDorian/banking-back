package com.example.app.bridge;

import com.example.app.model.Account;
import java.math.BigDecimal;

public class SMSNotificationSender implements NotificationSender {
    @Override
    public void send(String message, Account from, Account to, BigDecimal amount) {
        System.out.println("[SMS] " + message);
        System.out.println("Received " + amount + " LEI from " + from.getAccountNumber());
    }
}