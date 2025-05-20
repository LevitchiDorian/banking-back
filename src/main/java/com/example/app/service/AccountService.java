package com.example.app.service;

import com.example.app.decorator.InsuranceDecorator;
import com.example.app.decorator.PremiumBenefitsDecorator;
import com.example.app.dto.CreateAccountRequestDTO;
import com.example.app.dto.DbAccountResponseDTO;
import com.example.app.dto.DbTransactionResponseDTO;
import com.example.app.exception.InsufficientFundsException;
// import com.example.app.exception.UserAlreadyExistsException; // Comentat dacă nu e folosit direct aici
import com.example.app.exception.UnauthorizedOperationException;
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

@Service("accountServiceImpl")
public class AccountService {
    private final NotificationService notificationService;
    private final AccountAbstractFactory premiumAccountFactory;
    private final AccountAbstractFactory standardAccountFactory;
    private final AccountFactory checkingAccountFactory;
    private final AccountFactory savingsAccountFactory;
    private final Map<String, Account> accounts = new HashMap<>();

    private final DbAccountRepository dbAccountRepository;
    private final DbTransactionRepository dbTransactionRepository;
    private final UserRepository userRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final UserExtractServiceImpl userExtractService;

    @Autowired
    public AccountService(
            @Qualifier("emailNotificationService") NotificationService notificationService,
            @Qualifier("premiumAccountFactory") AccountAbstractFactory premiumAccountFactory,
            @Qualifier("standardAccountFactory") AccountAbstractFactory standardAccountFactory,
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

    // --- METODE EXISTENTE PENTRU CONTURI IN-MEMORY ---
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
        Account account = standardAccountFactory.createCheckingAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createStandardSavingsAccount(String accountNumber) {
        Account account = standardAccountFactory.createSavingsAccount(accountNumber);
        accounts.put(accountNumber, account);
        return account;
    }

    public Account createCheckingAccount(String accountNumber) {
        return createStandardCheckingAccount(accountNumber);
    }

    public Account createSavingsAccount(String accountNumber) {
        return createStandardSavingsAccount(accountNumber);
    }

    @Transactional
    public void transfer(Account from, Account to, BigDecimal amount) {
        // Validare fonduri pentru conturile in-memory (presupunând că metoda withdraw aruncă excepție)
        try {
            from.withdraw(amount);
        } catch (com.example.app.exception.InsufficientFundsException e) { // Folosește excepția ta specifică
            throw new InsufficientFundsException("Fonduri insuficiente în contul sursă (in-memory): " + from.getAccountNumber());
        }
        to.deposit(amount);
        notificationService.sendTransferNotification(from, to, amount);
    }

    public Account getAccountByNumber(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Contul in-memory nu a fost găsit: " + accountNumber);
        }
        return account;
    }

    public Account cloneAccount(String originalNumber, String newNumber) {
        Account original = accounts.get(originalNumber);
        if (original == null) throw new IllegalArgumentException("Contul in-memory nu a fost găsit: " + originalNumber);
        Account clone = original.clone(newNumber);
        accounts.put(newNumber, clone);
        return clone;
    }

    public Account addInsurance(Account account, BigDecimal benefit) {
        Account decorated = new InsuranceDecorator(account, benefit);
        accounts.put(account.getAccountNumber(), decorated);
        return decorated;
    }

    public Account addPremiumBenefits(Account account) {
        Account decorated = new PremiumBenefitsDecorator(account);
        accounts.put(account.getAccountNumber(), decorated);
        return decorated;
    }

    public Map<String, Account> getAllAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    // --- NOI METODE PENTRU INTERACȚIUNEA CU BAZA DE DATE ---
    private String generateUniqueAccountNumber() {
        String candidate;
        do {
            candidate = "MD" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        } while (dbAccountRepository.existsByAccountNumber(candidate));
        return candidate;
    }

    @Transactional
    public DbAccountResponseDTO createDbAccountForUser(String username, CreateAccountRequestDTO requestDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizator negăsit: " + username));

        AccountType accountType = accountTypeRepository.findByTypeName(requestDTO.getAccountTypeName())
                .orElseThrow(() -> new EntityNotFoundException("Tipul de cont negăsit: " + requestDTO.getAccountTypeName()));

        DbAccount newDbAccount = new DbAccount();
        newDbAccount.setAccountNumber(generateUniqueAccountNumber());
        newDbAccount.setUser(user);
        newDbAccount.setAccountType(accountType);
        newDbAccount.setBalance(requestDTO.getInitialDeposit());
        String currency = requestDTO.getCurrency() != null && !requestDTO.getCurrency().trim().isEmpty()
                ? requestDTO.getCurrency().toUpperCase()
                : "LEI";
        newDbAccount.setCurrency(currency);
        newDbAccount.setOpenedDate(LocalDateTime.now());

        if (accountType.getTypeName().toUpperCase().contains("PREMIUM")) {
            newDbAccount.setHasPremiumBenefits(true);
        } else {
            newDbAccount.setHasPremiumBenefits(false);
        }
        newDbAccount.setInsuranceBenefit(BigDecimal.ZERO);

        DbAccount savedAccount = dbAccountRepository.save(newDbAccount);

        if (requestDTO.getInitialDeposit() != null && requestDTO.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            DbTransaction initialDepositTransaction = new DbTransaction();
            initialDepositTransaction.setToAccount(savedAccount);
            initialDepositTransaction.setFromAccount(null);
            initialDepositTransaction.setAmount(requestDTO.getInitialDeposit());
            initialDepositTransaction.setCurrency(savedAccount.getCurrency()); // Setează moneda tranzacției
            initialDepositTransaction.setDescription("Depunere inițială");
            initialDepositTransaction.setTransactionType("DEPOSIT");
            initialDepositTransaction.setTimestamp(LocalDateTime.now());
            dbTransactionRepository.save(initialDepositTransaction);
        }
        return mapDbAccountToResponseDTO(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<DbAccountResponseDTO> getDbAccountsByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException("Utilizator negăsit: " + username);
        }
        List<DbAccount> userAccounts = dbAccountRepository.findByUserUsername(username);
        return userAccounts.stream()
                .map(this::mapDbAccountToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DbTransactionResponseDTO> getAllDbTransactionsByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException("Utilizator negăsit: " + username);
        }
        List<DbTransaction> transactions = dbTransactionRepository.findAllTransactionsByUsername(username);
        return transactions.stream()
                .map(this::mapDbTransactionToResponseDTO) // Aici se folosește metoda corectată
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DbTransactionResponseDTO> getDbTransactionsByAccountNumberForUser(String accountNumber, String username) {
        DbAccount account = dbAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Cont negăsit: " + accountNumber));

        if (!account.getUser().getUsername().equals(username)) {
            // Folosește o excepție mai specifică sau gestionează altfel (nu SecurityException direct)
            throw new UnauthorizedOperationException("Utilizatorul " + username + " nu este autorizat să vadă tranzacțiile pentru contul " + accountNumber);
        }

        List<DbTransaction> transactions = dbTransactionRepository.findAllByAccountNumber(accountNumber);
        return transactions.stream()
                .map(this::mapDbTransactionToResponseDTO) // Aici se folosește metoda corectată
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DbAccountResponseDTO getDbAccountDetails(String accountNumber, String username) {
        DbAccount account = dbAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Cont negăsit: " + accountNumber));

        if (!account.getUser().getUsername().equals(username)) {
            throw new UnauthorizedOperationException("Utilizatorul " + username + " nu este autorizat să vadă detaliile pentru contul " + accountNumber);
        }
        return mapDbAccountToResponseDTO(account);
    }

    private DbAccountResponseDTO mapDbAccountToResponseDTO(DbAccount dbAccount) {
        if (dbAccount == null) return null;
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

    // Metoda Corectată
    private DbTransactionResponseDTO mapDbTransactionToResponseDTO(DbTransaction dbTransaction) {
        if (dbTransaction == null) {
            return null;
        }
        String fromAccNum = dbTransaction.getFromAccount() != null ? dbTransaction.getFromAccount().getAccountNumber() : "EXTERNAL";
        String toAccNum = dbTransaction.getToAccount() != null ? dbTransaction.getToAccount().getAccountNumber() : "EXTERNAL";

        return new DbTransactionResponseDTO(
                dbTransaction.getId(),
                fromAccNum,
                toAccNum,
                dbTransaction.getAmount(),
                dbTransaction.getCurrency(), // Am adăugat dbTransaction.getCurrency()
                dbTransaction.getDescription(),
                dbTransaction.getTimestamp(),
                dbTransaction.getTransactionType()
        );
    }
}