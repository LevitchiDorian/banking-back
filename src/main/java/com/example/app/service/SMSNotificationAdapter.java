package com.example.app.service;

import com.example.app.bridge.NotificationSender;
import com.example.app.interfaces.NotificationService;
import com.example.app.model.Account;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("smsNotificationService")
public class SMSNotificationAdapter implements NotificationService {
    private final NotificationSender sender;

    public SMSNotificationAdapter(@Qualifier("smsSender") NotificationSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendTransferNotification(Account from, Account to, BigDecimal amount) {
        String message = "New transaction alert";
        sender.send(message, from, to, amount);
    }
}