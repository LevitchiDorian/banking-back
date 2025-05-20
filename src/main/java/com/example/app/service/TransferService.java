package com.example.app.service;

import com.example.app.dto.transfer.DomesticBankTransferRequestDTO;
import com.example.app.dto.transfer.IntrabankTransferRequestDTO;
import com.example.app.dto.transfer.OwnAccountTransferRequestDTO;
import com.example.app.dto.transfer.TransferResponseDTO;
import com.example.app.exception.AccountNotFoundException;
import com.example.app.exception.CurrencyConversionException;
import com.example.app.exception.InsufficientFundsException;
import com.example.app.exception.InvalidTransferException;
import com.example.app.exception.UnauthorizedOperationException;
import com.example.app.model.DbAccount;
import com.example.app.model.DbTransaction;
import com.example.app.model.User;
import com.example.app.repository.DbAccountRepository;
import com.example.app.repository.DbTransactionRepository;
import com.example.app.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class TransferService {

    private final DbAccountRepository dbAccountRepository;
    private final DbTransactionRepository dbTransactionRepository;
    private final UserRepository userRepository;
    private final ExchangeRateService exchangeRateService;

    private static final BigDecimal DOMESTIC_TRANSFER_FEE_PERCENTAGE = new BigDecimal("0.01"); // 1% comision

    @Autowired
    public TransferService(DbAccountRepository dbAccountRepository,
                           DbTransactionRepository dbTransactionRepository,
                           UserRepository userRepository,
                           ExchangeRateService exchangeRateService) {
        this.dbAccountRepository = dbAccountRepository;
        this.dbTransactionRepository = dbTransactionRepository;
        this.userRepository = userRepository;
        this.exchangeRateService = exchangeRateService;
    }

    // --- Transfer între Conturile Proprii ---
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @Retryable(value = { OptimisticLockException.class, DataAccessException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransferResponseDTO transferBetweenOwnAccounts(String currentUsername, OwnAccountTransferRequestDTO request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul curent nu a fost găsit: " + currentUsername));

        DbAccount sourceAccount = dbAccountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Contul sursă nu a fost găsit. ID: " + request.getFromAccountId()));

        DbAccount destinationAccount = dbAccountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Contul destinație nu a fost găsit. ID: " + request.getToAccountId()));

        if (!sourceAccount.getUser().getId().equals(user.getId()) || !destinationAccount.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Utilizatorul nu este proprietarul ambelor conturi pentru transferul propriu.");
        }
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new InvalidTransferException("Contul sursă și cel destinație nu pot fi identice.");
        }

        BigDecimal amountToDebit = request.getAmount();
        BigDecimal amountToCredit;
        String finalTransactionDescription = "Transfer între conturi proprii";
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            finalTransactionDescription += ": " + request.getDescription();
        }

        if (!sourceAccount.getCurrency().equalsIgnoreCase(destinationAccount.getCurrency())) {
            try {
                BigDecimal rate = exchangeRateService.getExchangeRate(sourceAccount.getCurrency(), destinationAccount.getCurrency());
                amountToCredit = exchangeRateService.convert(amountToDebit, sourceAccount.getCurrency(), destinationAccount.getCurrency());
                finalTransactionDescription += String.format(" (Suma originală: %.2f %s, Suma creditată: %.2f %s. Curs: 1 %s = %.4f %s)",
                        amountToDebit, sourceAccount.getCurrency(),
                        amountToCredit, destinationAccount.getCurrency(),
                        sourceAccount.getCurrency(), rate, destinationAccount.getCurrency());
            } catch (IllegalArgumentException e) {
                throw new CurrencyConversionException("Conversia valutară pentru transferul propriu nu a putut fi efectuată: " + e.getMessage());
            }
        } else {
            amountToCredit = amountToDebit;
        }

        if (sourceAccount.getBalance().compareTo(amountToDebit) < 0) {
            throw new InsufficientFundsException("Fonduri insuficiente în contul sursă: " + sourceAccount.getAccountNumber() + ". Necesitați: " + amountToDebit.setScale(2, RoundingMode.HALF_UP) + " " + sourceAccount.getCurrency());
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amountToDebit));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amountToCredit));

        dbAccountRepository.save(sourceAccount);
        dbAccountRepository.save(destinationAccount);

        createAndSaveTransaction(sourceAccount, destinationAccount, amountToDebit, sourceAccount.getCurrency(),
                "OWN_ACCOUNT_TRANSFER", finalTransactionDescription);

        return new TransferResponseDTO("Transfer între conturi proprii efectuat cu succes.");
    }

    // --- Transfer Intrabancar (fără verificare existență cont destinație explicită AICI, doar debitare) ---
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @Retryable(value = { OptimisticLockException.class, DataAccessException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransferResponseDTO transferToIntrabankAccount(String currentUsername, IntrabankTransferRequestDTO request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul curent nu a fost găsit: " + currentUsername));

        DbAccount sourceAccount = dbAccountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Contul sursă nu a fost găsit. ID: " + request.getFromAccountId()));

        if (!sourceAccount.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Utilizatorul nu este proprietarul contului sursă pentru transferul intrabancar.");
        }
        if (sourceAccount.getAccountNumber().equalsIgnoreCase(request.getToIban())) {
            throw new InvalidTransferException("Contul sursă și IBAN-ul destinație (intrabancar) sunt identice.");
        }

        BigDecimal requestedAmountInTransferCurrency = request.getAmount();
        String transferCurrency = request.getCurrency().toUpperCase();
        BigDecimal amountToDebitFromSource;

        String finalTransactionDescription = "Transfer intrabancar către IBAN " + request.getToIban() + " (Beneficiar: " + request.getBeneficiaryName() + ")";
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            finalTransactionDescription += ": " + request.getDescription();
        }

        if (!sourceAccount.getCurrency().equalsIgnoreCase(transferCurrency)) {
            try {
                BigDecimal rate = exchangeRateService.getExchangeRate(transferCurrency, sourceAccount.getCurrency());
                amountToDebitFromSource = exchangeRateService.convert(requestedAmountInTransferCurrency, transferCurrency, sourceAccount.getCurrency());
                finalTransactionDescription += String.format(" (Valoare debitată: %.2f %s pentru transfer de %.2f %s. Curs: 1 %s = %.4f %s)",
                        amountToDebitFromSource, sourceAccount.getCurrency(),
                        requestedAmountInTransferCurrency, transferCurrency,
                        transferCurrency, rate, sourceAccount.getCurrency());
            } catch (IllegalArgumentException e) {
                throw new CurrencyConversionException("Conversia valutară pentru debitare (intrabancar) nu a putut fi efectuată: " + e.getMessage());
            }
        } else {
            amountToDebitFromSource = requestedAmountInTransferCurrency;
        }

        if (sourceAccount.getBalance().compareTo(amountToDebitFromSource) < 0) {
            throw new InsufficientFundsException("Fonduri insuficiente în contul sursă: " + sourceAccount.getAccountNumber() + ". Necesitați: " + amountToDebitFromSource.setScale(2, RoundingMode.HALF_UP) + " " + sourceAccount.getCurrency());
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amountToDebitFromSource));
        dbAccountRepository.save(sourceAccount);

        // Pentru acest model simplificat de transfer "intrabancar", nu credităm un cont destinație specific din DbAccount.
        // Tranzacția este înregistrată ca o ieșire către un IBAN (care se presupune a fi în aceeași bancă).
        createAndSaveTransaction(sourceAccount, null, requestedAmountInTransferCurrency, transferCurrency,
                "INTRABANK_TRANSFER_SENT", finalTransactionDescription);

        return new TransferResponseDTO("Transferul către IBAN-ul " + request.getToIban() + " a fost inițiat.");
    }

    // --- Transfer Interbancar Național (cu Comision) ---
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    @Retryable(value = { OptimisticLockException.class, DataAccessException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransferResponseDTO transferToDomesticBankAccount(String currentUsername, DomesticBankTransferRequestDTO request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizatorul curent nu a fost găsit: " + currentUsername));

        DbAccount sourceAccount = dbAccountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Contul sursă nu a fost găsit. ID: " + request.getFromAccountId()));

        if (!sourceAccount.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Utilizatorul nu este proprietarul contului sursă pentru transferul domestic.");
        }

        BigDecimal amountToSend = request.getAmount(); // Suma pe care utilizatorul vrea să o primească destinatarul
        String transferCurrency = request.getCurrency().toUpperCase();

        BigDecimal commissionAmount = amountToSend.multiply(DOMESTIC_TRANSFER_FEE_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmountInTransferCurrency = amountToSend.add(commissionAmount);

        BigDecimal amountToDebitFromSource;
        String finalTransactionDescription = "Transfer către " + request.getBeneficiaryBankName() +
                " (Beneficiar: " + request.getBeneficiaryName() +
                ", IBAN: " + request.getToIban() + ")";
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            finalTransactionDescription += ": " + request.getDescription();
        }
        finalTransactionDescription += String.format(". Suma transferată: %.2f %s. Comision (%.0f%%): %.2f %s. Total debitat (echivalent): ",
                amountToSend, transferCurrency,
                DOMESTIC_TRANSFER_FEE_PERCENTAGE.multiply(new BigDecimal("100")), // Afișează procentul
                commissionAmount, transferCurrency);

        if (!sourceAccount.getCurrency().equalsIgnoreCase(transferCurrency)) {
            try {
                BigDecimal rate = exchangeRateService.getExchangeRate(transferCurrency, sourceAccount.getCurrency());
                amountToDebitFromSource = exchangeRateService.convert(totalAmountInTransferCurrency, transferCurrency, sourceAccount.getCurrency());
                finalTransactionDescription += String.format("%.2f %s (Curs: 1 %s = %.4f %s)",
                        amountToDebitFromSource, sourceAccount.getCurrency(),
                        transferCurrency, rate, sourceAccount.getCurrency());
            } catch (IllegalArgumentException e) {
                throw new CurrencyConversionException("Conversia valutară pentru debitare (transfer domestic) nu a putut fi efectuată: " + e.getMessage());
            }
        } else {
            amountToDebitFromSource = totalAmountInTransferCurrency;
            finalTransactionDescription += String.format("%.2f %s", amountToDebitFromSource, sourceAccount.getCurrency());
        }

        if (sourceAccount.getBalance().compareTo(amountToDebitFromSource) < 0) {
            BigDecimal commissionInSourceCurrency = sourceAccount.getCurrency().equalsIgnoreCase(transferCurrency) ?
                    commissionAmount :
                    exchangeRateService.convert(commissionAmount, transferCurrency, sourceAccount.getCurrency());
            throw new InsufficientFundsException(String.format(
                    "Fonduri insuficiente în contul sursă %s. Necesitați: %.2f %s (include comision de %.2f %s). Sold disponibil: %.2f %s",
                    sourceAccount.getAccountNumber(),
                    amountToDebitFromSource.setScale(2, RoundingMode.HALF_UP), sourceAccount.getCurrency(),
                    commissionInSourceCurrency.setScale(2, RoundingMode.HALF_UP), sourceAccount.getCurrency(),
                    sourceAccount.getBalance().setScale(2, RoundingMode.HALF_UP), sourceAccount.getCurrency()
            ));
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amountToDebitFromSource));
        dbAccountRepository.save(sourceAccount);

        createAndSaveTransaction(sourceAccount, null, amountToSend, transferCurrency,
                "DOMESTIC_BANK_TRANSFER", finalTransactionDescription);

        // Dacă dorești să înregistrezi comisionul ca o tranzacție separată:
        // BigDecimal commissionInSourceCurrency = sourceAccount.getCurrency().equalsIgnoreCase(transferCurrency) ?
        //                                         commissionAmount :
        //                                         exchangeRateService.convert(commissionAmount, transferCurrency, sourceAccount.getCurrency());
        // createAndSaveTransaction(sourceAccount, null, commissionInSourceCurrency, sourceAccount.getCurrency(),
        //                          "TRANSFER_FEE", "Comision transfer domestic către " + request.getToIban());

        return new TransferResponseDTO("Transfer interbancar național inițiat cu succes. Suma totală debitată (inclusiv comision): " + amountToDebitFromSource.setScale(2, RoundingMode.HALF_UP) + " " + sourceAccount.getCurrency());
    }

    // Helper pentru crearea și salvarea obiectului DbTransaction
    // Asigură-te că entitatea DbTransaction are un câmp 'currency' și setter/getter pentru el.
    private DbTransaction createAndSaveTransaction(DbAccount fromAccount,
                                                   DbAccount toAccount,
                                                   BigDecimal amount,
                                                   String currency,
                                                   String transactionType,
                                                   String description) {
        DbTransaction transaction = new DbTransaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount); // Poate fi null pentru transferuri externe
        transaction.setAmount(amount);
        transaction.setCurrency(currency.toUpperCase()); // Stochează moneda tranzacției
        transaction.setTransactionType(transactionType);
        transaction.setDescription(description);
        transaction.setTimestamp(LocalDateTime.now());
        return dbTransactionRepository.save(transaction);
    }
}