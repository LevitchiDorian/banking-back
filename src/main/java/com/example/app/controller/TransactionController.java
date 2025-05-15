package com.example.app.controller;

import com.example.app.dto.DbTransactionResponseDTO;
import com.example.app.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final AccountService accountServiceImpl; // Folosim implementarea directă

    @Autowired
    public TransactionController(@Qualifier("accountServiceImpl") AccountService accountServiceImpl) {
        this.accountServiceImpl = accountServiceImpl;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getName();
    }

    @GetMapping
    public ResponseEntity<List<DbTransactionResponseDTO>> getAllUserTransactions() {
        String username = getCurrentUsername();
        List<DbTransactionResponseDTO> transactions = accountServiceImpl.getAllDbTransactionsByUsername(username);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<DbTransactionResponseDTO>> getTransactionsForUserAccount(@PathVariable String accountNumber) {
        String username = getCurrentUsername();
        List<DbTransactionResponseDTO> transactions = accountServiceImpl.getDbTransactionsByAccountNumberForUser(accountNumber, username);
        return ResponseEntity.ok(transactions);
    }
}