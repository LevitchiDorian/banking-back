package com.example.app.flyweight;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class AccountTypeFlyweight {
    private static final Map<String, AccountProperties> accountTypes = new HashMap<>();

    public static AccountProperties getAccountType(String type, BigDecimal overdraft, BigDecimal interestRate) {
        String key = type + overdraft + interestRate; // Cheie unică pentru fiecare combinație
        return accountTypes.computeIfAbsent(key, k ->
                new AccountProperties(overdraft, interestRate) // Creează o instanță nouă doar dacă nu există deja
        );
    }
}