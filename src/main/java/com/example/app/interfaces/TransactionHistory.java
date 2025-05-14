package com.example.app.interfaces;
import com.example.app.model.Transaction;
import java.util.List;

public interface TransactionHistory {
    List<Transaction> getTransactionHistory();
}