package com.example.app.dto.transfer;

import com.example.app.dto.DbTransactionResponseDTO; // Presupunând că ai acest DTO pentru afișarea tranzacțiilor

public class TransferResponseDTO {
    private String message;
    private DbTransactionResponseDTO transactionDetails; // Opțional, detaliile tranzacției create

    public TransferResponseDTO(String message) {
        this.message = message;
    }

    public TransferResponseDTO(String message, DbTransactionResponseDTO transactionDetails) {
        this.message = message;
        this.transactionDetails = transactionDetails;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public DbTransactionResponseDTO getTransactionDetails() { return transactionDetails; }
    public void setTransactionDetails(DbTransactionResponseDTO transactionDetails) { this.transactionDetails = transactionDetails; }
}