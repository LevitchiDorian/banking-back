package com.example.app.decorator;

import com.example.app.model.Account;
import com.example.app.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InsuranceDecorator extends AccountDecorator {
    private final BigDecimal insuranceBenefit;

    public InsuranceDecorator(Account decoratedAccount, BigDecimal insuranceBenefit) {
        super(decoratedAccount);
        this.insuranceBenefit = insuranceBenefit;
    }

    // Suprascriere applyInterest() cu adăugare beneficii
    @Override
    public void applyInterest() {
        super.applyInterest(); // Apelează implementarea din AccountDecorator
        decoratedAccount.deposit(insuranceBenefit);
        super.addTransaction(new Transaction(
                LocalDateTime.now(),
                insuranceBenefit,
                "Insurance Benefit"
        ));
    }

    // Restul metodelor rămân neschimbate
    @Override
    public Account clone(String newAccountNumber) {
        return new InsuranceDecorator(
                decoratedAccount.clone(newAccountNumber),
                insuranceBenefit
        );
    }

    @Override
    public void deposit(BigDecimal amount) {
        decoratedAccount.deposit(amount);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        decoratedAccount.withdraw(amount);
    }
}