package com.example.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final LocalDateTime timestamp;
    private final BigDecimal amount;
    private final String description;

    public Transaction(LocalDateTime timestamp, BigDecimal amount, String description) {
        this.timestamp = timestamp;
        this.amount = amount;
        this.description = description;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
}
