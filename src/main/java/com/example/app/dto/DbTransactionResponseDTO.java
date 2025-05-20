package com.example.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DbTransactionResponseDTO {
    private Long id;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency; // Noul câmp adăugat
    private String description;
    private LocalDateTime timestamp;
    private String transactionType;

    // Constructors
    public DbTransactionResponseDTO() {
    }

    // Constructor actualizat pentru a include moneda
    public DbTransactionResponseDTO(Long id, String fromAccountNumber, String toAccountNumber,
                                    BigDecimal amount, String currency, String description,
                                    LocalDateTime timestamp, String transactionType) {
        this.id = id;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.currency = currency; // Inițializează noul câmp
        this.description = description;
        this.timestamp = timestamp;
        this.transactionType = transactionType;
    }

    // Getters and Setters (Lombok le-ar genera, dar le punem explicit dacă nu folosești @Data pe DTO)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; } // Getter pentru noul câmp
    public void setCurrency(String currency) { this.currency = currency; } // Setter pentru noul câmp

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
}