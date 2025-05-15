package com.example.app.controller;

import com.example.app.dto.CreateAccountRequestDTO;
import com.example.app.dto.DbAccountResponseDTO;
import com.example.app.model.Account; // Pentru conturile in-memory
import com.example.app.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Am scos @RequestMapping("/accounts") de aici pentru a putea avea prefixe diferite
public class AccountController {

    private final AccountService accountService; // Acesta va fi proxy-ul "accountService"
    private final AccountService accountServiceImpl; // Acesta va fi implementarea directă "accountServiceImpl"

    @Autowired
    public AccountController(
            @Qualifier("accountService") AccountService accountService, // Proxy pentru metodele vechi/demo
            @Qualifier("accountServiceImpl") AccountService accountServiceImpl // Implementarea directă pentru noile metode DB
    ) {
        this.accountService = accountService;
        this.accountServiceImpl = accountServiceImpl;
    }

    // --- ENDPOINT-URI EXISTENTE PENTRU CONTURI IN-MEMORY (DEMO PATTERNS) ---
    // Acestea rămân sub /accounts și folosesc bean-ul "accountService" (proxy-ul)
    @GetMapping("/accounts/create-checking")
    public String createCheckingAccount(@RequestParam String accountNumber) {
        Account account = accountService.createCheckingAccount(accountNumber); // Folosește proxy-ul
        return "Cont checking (in-memory) creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }

    @GetMapping("/accounts/create-savings")
    public String createSavingsAccount(@RequestParam String accountNumber) {
        Account account = accountService.createSavingsAccount(accountNumber); // Folosește proxy-ul
        return "Cont savings (in-memory) creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }

    @GetMapping("/accounts/create-premium-checking")
    public String createPremiumCheckingAccount(@RequestParam String accountNumber) {
        Account account = accountService.createPremiumCheckingAccount(accountNumber); // Folosește proxy-ul
        return "Cont checking premium (in-memory) creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }

    @GetMapping("/accounts/create-premium-savings")
    public String createPremiumSavingsAccount(@RequestParam String accountNumber) {
        Account account = accountService.createPremiumSavingsAccount(accountNumber); // Folosește proxy-ul
        return "Cont savings premium (in-memory) creat: " + account.getClass().getSimpleName() + " cu numărul " + accountNumber;
    }
    // --- SFÂRȘIT ENDPOINT-URI PENTRU CONTURI IN-MEMORY ---


    // --- NOI ENDPOINT-URI PENTRU CONTURI DIN BAZA DE DATE ---
    // Acestea vor fi sub /api/v1/db-accounts și folosesc "accountServiceImpl"

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

    @PostMapping("/api/v1/db-accounts")
    public ResponseEntity<DbAccountResponseDTO> createDatabaseAccount(@Valid @RequestBody CreateAccountRequestDTO requestDTO) {
        String username = getCurrentUsername();
        DbAccountResponseDTO createdAccount = accountServiceImpl.createDbAccountForUser(username, requestDTO);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/db-accounts")
    public ResponseEntity<List<DbAccountResponseDTO>> getUserDatabaseAccounts() {
        String username = getCurrentUsername();
        List<DbAccountResponseDTO> accounts = accountServiceImpl.getDbAccountsByUsername(username);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/api/v1/db-accounts/{accountNumber}")
    public ResponseEntity<DbAccountResponseDTO> getDatabaseAccountDetails(@PathVariable String accountNumber) {
        String username = getCurrentUsername();
        DbAccountResponseDTO accountDetails = accountServiceImpl.getDbAccountDetails(accountNumber, username);
        return ResponseEntity.ok(accountDetails);
    }
}