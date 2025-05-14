package com.example.app.exception;

public class HighRiskTransactionException extends RuntimeException {
    public HighRiskTransactionException(String message) {
        super(message);
    }
}