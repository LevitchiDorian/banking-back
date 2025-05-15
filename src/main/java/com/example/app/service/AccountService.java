package com.example.app.service;

import com.example.app.decorator.InsuranceDecorator;
import com.example.app.decorator.PremiumBenefitsDecorator;
import com.example.app.dto.CreateAccountRequestDTO;
import com.example.app.dto.DbAccountResponseDTO;
import com.example.app.dto.DbTransactionResponseDTO;
import com.example.app.exception.InsufficientFundsException; // Asigurați-vă că acest import există dacă este necesar
import com.example.app.exception.UserAlreadyExistsException; // Asigurați-vă că acest import există dacă este necesar
import com.example.app.factory.AccountFactory;
import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.interfaces.NotificationService;
import com.example.app.model.Account;
import com.example.app.model.AccountType;
import com.example.app.model.DbAccount;
import com.example.app.model.DbTransaction;
import com.example.app.model.User;
import com.example.app.repository.AccountTypeRepository;
import com.example.app.repository.DbAccountRepository;
import com.example.app.repository.DbTransactionRepository;
import com.example.app.repository.UserRepository;
import com.example.app.util.UserExtractServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("accountServiceImpl") // Qualifier existent
public class AccountService {
    private final NotificationService notificationService; // Pentru conturile in-memory
    private final AccountAbstractFactory premiumAccountFactory; // Pentru conturile in-memory
    private final AccountAbstractFactory standardAccountFactory; // Pentru conturile in-memory (am adăugat și standard)
    private final AccountFactory checkingAccountFactory; // Pentru conturile in-memory
    private final AccountFactory savingsAccountFactory; // Pentru conturile in-memory
    private final Map<String, Account> accounts = new HashMap<>(); // Colecție pentru conturi in-memory

    // Noi dependențe pentru interacțiunea cu BD
    private final DbAccountRepository dbAccountRepository;
    private final DbTransactionRepository dbTransactionRepository;
    private final UserRepository userRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final UserExtractServiceImpl userExtractService; // Pentru a obține userul curent

    @Autowired
    public AccountService(
            @Qualifier("emailNotificationService") NotificationService notificationService,
            @Qualifier("premiumAccountFactory") AccountAbstractFactory premiumAccountFactory,
            @Qualifier("standardAccountFactory") AccountAbstractFactory standardAccountFactory, // Adăugat standardFactory
            @Qualifier("checkingAccountFactory") AccountFactory checkingAccountFactory,
            @Qualifier("savingsAccountFactory") AccountFactory savingsAccountFactory,
            DbAccountRepository dbAccountRepository,
            DbTransactionRepository dbTransactionRepository,
            UserRepository userRepository,
            AccountTypeRepository accountTypeRepository,
            UserExtractServiceImpl userExtractService
    ) {
        this.notificationService = notificationService;
        this.premiumAccountFactory = premiumAccountFactory;
        this.standardAccountFactory = standardAccountFactory;
        this.checkingAccountFactory = checkingAccountFactory;
        this.savingsAccountFactory = savingsAccountFactory;
        this.dbAccountRepository = dbAccountRepository;
        this.dbTransactionRepository = dbTransactionRepository;
        this.userRepository = userRepository;
        this.accountTypeRepository = accountTypeRepository;
        this.userExtractService = userExtractService;
    }

    // --- METODE EXISTENTE PENTRU CONTURI IN-MEMORY (Factory, Decorator, etc.) ---
    // Aceste metode rămân neschimbate și operează pe colecția `accounts`

    public Account createPremiumCheckingAccount(String accountNumber) {
        Account account = premiumAccountFactory.createCheckingAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createPremiumSavingsAccount(String accountNumber) {
        Account account = premiumAccountFactory.createSavingsAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createStandardCheckingAccount(String accountNumber) {
        // Presupunând că standardAccountFactory poate crea conturi checking standard
        // sau folosim checkingAccountFactory direct dacă e mai specific
        Account account = standardAccountFactory.createCheckingAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createStandardSavingsAccount(String accountNumber) {
        // Presupunând că standardAccountFactory poate crea conturi savings standard
        // sau folosim savingsAccountFactory direct dacă e mai specific
        Account account = standardAccountFactory.createSavingsAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    // Metodă generică pentru controller-ul vechi
    public Account createCheckingAccount(String accountNumber) {
        return createStandardCheckingAccount(accountNumber);
    }

    public Account createSavingsAccount(String accountNumber) {
        return createStandardSavingsAccount(accountNumber);
    }

    @Transactional // Pentru operațiuni in-memory care ar simula transferul
    public void transfer(Account from, Account to, BigDecimal amount) {
        from.withdraw(amount); // Aruncă excepție dacă nu sunt fonduri (depinde de implementare)
        to.deposit(amount);
        notificationService.sendTransferNotification(from, to, amount);
    }

    public Account getAccountByNumber(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("In-memory account not found: " + accountNumber);
        }
        return account;
    }

    public Account cloneAccount(String originalNumber, String newNumber) {
        Account original = accounts.get(originalNumber);
        if (original == null) throw new IllegalArgumentException("In-memory account not found: " + originalNumber);
        Account clone = original.clone(newNumber);
        accounts.put(newNumber, clone);
        return clone;
    }

    public Account addInsurance(Account account, BigDecimal benefit) {
        Account decorated = new InsuranceDecorator(account, benefit);
        accounts.put(account.getAccountNumber(), decorated); // Suprascrie contul original cu cel decorat
        return decorated;
    }

    public Account addPremiumBenefits(Account account) {
        Account decorated = new PremiumBenefitsDecorator(account);
        accounts.put(account.getAccountNumber(), decorated); // Suprascrie contul original cu cel decorat
        return decorated;
    }

    public Map<String, Account> getAllAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    // --- SFÂRȘIT METODE PENTRU CONTURI IN-MEMORY ---

    // --- NOI METODE PENTRU INTERACȚIUNEA CU BAZA DE DATE ---

    private String generateUniqueAccountNumber() {
        // O implementare simplă pentru a genera un număr de cont unic.
        // Într-o aplicație reală, ar trebui să fie mai robust (ex: prefix bancar, validare, etc.)
        String candidate;
        do {
            candidate = "MD" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        } while (dbAccountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    @Transactional
    public DbAccountResponseDTO createDbAccountForUser(String username, CreateAccountRequestDTO requestDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        AccountType accountType = accountTypeRepository.findByTypeName(requestDTO.getAccountTypeName())
                .orElseThrow(() -> new EntityNotFoundException("AccountType not found: " + requestDTO.getAccountTypeName()));

        DbAccount newDbAccount = new DbAccount();
        newDbAccount.setAccountNumber(generateUniqueAccountNumber());
        newDbAccount.setUser(user);
        newDbAccount.setAccountType(accountType);
        newDbAccount.setBalance(requestDTO.getInitialDeposit());
        newDbAccount.setCurrency(requestDTO.getCurrency() != null ? requestDTO.getCurrency() : "LEI");
        newDbAccount.setOpenedDate(LocalDateTime.now());
        // Setare default pentru hasPremiumBenefits și insuranceBenefit dacă este cazul, bazat pe accountType
        // De exemplu, dacă e premium savings, setăm hasPremiumBenefits = true
        // Acest lucru poate fi extins
        if (accountType.getTypeName().toUpperCase().contains("PREMIUM")) {
            newDbAccount.setHasPremiumBenefits(true);
        } else {
            newDbAccount.setHasPremiumBenefits(false);
        }
        newDbAccount.setInsuranceBenefit(BigDecimal.ZERO); // Sau altă valoare default

        DbAccount savedAccount = dbAccountRepository.save(newDbAccount);

        // Dacă există un depozit inițial, creăm o tranzacție de tip DEPOSIT
        if (requestDTO.getInitialDeposit() != null && requestDTO.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            DbTransaction initialDepositTransaction = new DbTransaction();
            initialDepositTransaction.setToAccount(savedAccount); // Depozit în contul nou creat
            initialDepositTransaction.setFromAccount(null); // Sursa este externă/numerar
            initialDepositTransaction.setAmount(requestDTO.getInitialDeposit());
            initialDepositTransaction.setDescription("Initial deposit");
            initialDepositTransaction.setTransactionType("DEPOSIT");
            initialDepositTransaction.setTimestamp(LocalDateTime.now());
            dbTransactionRepository.save(initialDepositTransaction);
        }

        return mapDbAccountToResponseDTO(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<DbAccountResponseDTO> getDbAccountsByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        List<DbAccount> userAccounts = dbAccountRepository.findByUserUsername(username);
        return userAccounts.stream()
                .map(this::mapDbAccountToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DbTransactionResponseDTO> getAllDbTransactionsByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        List<DbTransaction> transactions = dbTransactionRepository.findAllTransactionsByUsername(username);
        return transactions.stream()
                .map(this::mapDbTransactionToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DbTransactionResponseDTO> getDbTransactionsByAccountNumberForUser(String accountNumber, String username) {
        DbAccount account = dbAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountNumber));

        // Verifică dacă contul aparține utilizatorului specificat
        if (!account.getUser().getUsername().equals(username)) {
            throw new SecurityException("User " + username + " is not authorized to view transactions for account " + accountNumber);
        }

        List<DbTransaction> transactions = dbTransactionRepository.findAllByAccountNumber(accountNumber);
        return transactions.stream()
                .map(this::mapDbTransactionToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DbAccountResponseDTO getDbAccountDetails(String accountNumber, String username) {
        DbAccount account = dbAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountNumber));

        if (!account.getUser().getUsername().equals(username)) {
            throw new SecurityException("User " + username + " is not authorized to view details for account " + accountNumber);
        }
        return mapDbAccountToResponseDTO(account);
    }

    // Helper methods pentru maparea Entităților la DTO-uri
    private DbAccountResponseDTO mapDbAccountToResponseDTO(DbAccount dbAccount) {
        return new DbAccountResponseDTO(
                dbAccount.getId(),
                dbAccount.getAccountNumber(),
                dbAccount.getUser().getUsername(),
                dbAccount.getAccountType().getTypeName(),
                dbAccount.getBalance(),
                dbAccount.getCurrency(),
                dbAccount.getInsuranceBenefit(),
                dbAccount.getHasPremiumBenefits(),
                dbAccount.getOpenedDate()
        );
    }

    private DbTransactionResponseDTO mapDbTransactionToResponseDTO(DbTransaction dbTransaction) {
        String fromAccNum = dbTransaction.getFromAccount() != null ? dbTransaction.getFromAccount().getAccountNumber() : "EXTERNAL";
        String toAccNum = dbTransaction.getToAccount() != null ? dbTransaction.getToAccount().getAccountNumber() : "EXTERNAL";

        return new DbTransactionResponseDTO(
                dbTransaction.getId(),
                fromAccNum,
                toAccNum,
                dbTransaction.getAmount(),
                dbTransaction.getDescription(),
                dbTransaction.getTimestamp(),
                dbTransaction.getTransactionType()
        );
    }

    // --- SFÂRȘIT NOI METODE PENTRU BAZA DE DATE ---
}