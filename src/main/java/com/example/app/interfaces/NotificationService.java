package com.example.app.interfaces;
import com.example.app.model.Account;
import java.math.BigDecimal;

public interface NotificationService {
    void sendTransferNotification(Account from, Account to, BigDecimal amount);
}
