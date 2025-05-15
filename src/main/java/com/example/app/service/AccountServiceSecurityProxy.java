package com.example.app.service;

import com.example.app.exception.HighRiskTransactionException;
import com.example.app.factory.AccountFactory;
import com.example.app.interfaces.AccountAbstractFactory;
import com.example.app.interfaces.NotificationService;
import com.example.app.model.Account;
// Importurile necesare pentru dependențele constructorului superclasei
import com.example.app.repository.AccountTypeRepository;
import com.example.app.repository.DbAccountRepository;
import com.example.app.repository.DbTransactionRepository;
import com.example.app.repository.UserRepository;
import com.example.app.util.UserExtractServiceImpl;
// DTO-uri și alte clase dacă proxy-ul ar suprascrie metode care le folosesc
import com.example.app.dto.CreateAccountRequestDTO;
import com.example.app.dto.DbAccountResponseDTO;
import com.example.app.dto.DbTransactionResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service("accountService") // Acesta este proxy-ul, care înlocuiește implementarea originală ca bean principal "accountService"
public class AccountServiceSecurityProxy extends AccountService {
    private static final BigDecimal HIGH_RISK_LIMIT = new BigDecimal("10000");
    private final AccountService realService; // Acesta este @Qualifier("accountServiceImpl")

    @Autowired
    public AccountServiceSecurityProxy(
            // Injectează serviciul real pe care acest proxy îl va "împacheta"
            @Qualifier("accountServiceImpl") AccountService realService,

            // Injectează toate dependențele necesare pentru constructorul clasei părinte (AccountService)
            @Qualifier("emailNotificationService") NotificationService notificationService,
            @Qualifier("premiumAccountFactory") AccountAbstractFactory premiumAccountFactory,
            @Qualifier("standardAccountFactory") AccountAbstractFactory standardAccountFactory, // Adăugat
            @Qualifier("checkingAccountFactory") AccountFactory checkingAccountFactory,
            @Qualifier("savingsAccountFactory") AccountFactory savingsAccountFactory,
            DbAccountRepository dbAccountRepository,                           // Adăugat
            DbTransactionRepository dbTransactionRepository,                 // Adăugat
            UserRepository userRepository,                                     // Adăugat
            AccountTypeRepository accountTypeRepository,                     // Adăugat
            UserExtractServiceImpl userExtractService                        // Adăugat
    ) {
        // Apelează constructorul clasei părinte cu toate argumentele necesare
        super(
                notificationService,
                premiumAccountFactory,
                standardAccountFactory, // Transmis corect
                checkingAccountFactory,
                savingsAccountFactory,
                dbAccountRepository,    // Transmis corect
                dbTransactionRepository,// Transmis corect
                userRepository,         // Transmis corect
                accountTypeRepository,  // Transmis corect
                userExtractService      // Transmis corect
        );
        this.realService = realService; // Păstrează referința la serviciul real
    }

    // Suprascrie metoda transfer pentru a adăuga logica de securitate
    @Override
    @Transactional // Este bine ca și metoda din proxy să fie transactională dacă metoda suprascrisă este
    public void transfer(Account from, Account to, BigDecimal amount) {
        if (amount.compareTo(HIGH_RISK_LIMIT) > 0) {
            throw new HighRiskTransactionException("Tranzacții peste 10,000 LEI necesită aprobare manager!");
        }
        // Apelează metoda transfer din clasa părinte (care este logica de bază a AccountService)
        // sau, mai curat într-un proxy care împachetează, deleagă la 'realService'
        // super.transfer(from, to, amount); // Aceasta apelează AccountService.transfer() din instanța proxy
        realService.transfer(from, to, amount); // Aceasta apelează metoda din implementarea concretă 'accountServiceImpl'
    }

    // Pentru toate celelalte metode din AccountService pe care proxy-ul nu le
    // modifică specific, ar trebui să delegăm apelul către 'realService'.
    // Altfel, dacă nu sunt suprascrise, se vor executa metodele din clasa părinte
    // AccountService (din instanța proxy-ului), ceea ce este ok dacă 'realService' este chiar
    // implementarea logicii de bază.

    // Exemplu de delegare pentru alte metode (dacă nu se dorește comportamentul default al superclasei)
    // Trebuie adăugate suprascrieri pentru TOATE metodele publice din AccountService
    // pe care doriți să le expuneți prin proxy și care nu sunt `transfer`.

    @Override
    public Account createPremiumCheckingAccount(String accountNumber) {
        return realService.createPremiumCheckingAccount(accountNumber);
    }

    @Override
    public Account createPremiumSavingsAccount(String accountNumber) {
        return realService.createPremiumSavingsAccount(accountNumber);
    }

    @Override
    public Account createStandardCheckingAccount(String accountNumber) {
        return realService.createStandardCheckingAccount(accountNumber);
    }

    @Override
    public Account createStandardSavingsAccount(String accountNumber) {
        return realService.createStandardSavingsAccount(accountNumber);
    }

    @Override
    public Account createCheckingAccount(String accountNumber) {
        return realService.createCheckingAccount(accountNumber);
    }

    @Override
    public Account createSavingsAccount(String accountNumber) {
        return realService.createSavingsAccount(accountNumber);
    }

    @Override
    public Account getAccountByNumber(String accountNumber) {
        return realService.getAccountByNumber(accountNumber);
    }

    @Override
    public Account cloneAccount(String originalNumber, String newNumber) {
        return realService.cloneAccount(originalNumber, newNumber);
    }

    @Override
    public Account addInsurance(Account account, BigDecimal benefit) {
        return realService.addInsurance(account, benefit);
    }

    @Override
    public Account addPremiumBenefits(Account account) {
        return realService.addPremiumBenefits(account);
    }

    @Override
    public Map<String, Account> getAllAccounts() {
        return realService.getAllAccounts();
    }

    // Metode pentru DB
    @Override
    @Transactional
    public DbAccountResponseDTO createDbAccountForUser(String username, CreateAccountRequestDTO requestDTO) {
        return realService.createDbAccountForUser(username, requestDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DbAccountResponseDTO> getDbAccountsByUsername(String username) {
        return realService.getDbAccountsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DbTransactionResponseDTO> getAllDbTransactionsByUsername(String username) {
        return realService.getAllDbTransactionsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DbTransactionResponseDTO> getDbTransactionsByAccountNumberForUser(String accountNumber, String username) {
        return realService.getDbTransactionsByAccountNumberForUser(accountNumber, username);
    }

    @Override
    @Transactional(readOnly = true)
    public DbAccountResponseDTO getDbAccountDetails(String accountNumber, String username) {
        return realService.getDbAccountDetails(accountNumber, username);
    }
}