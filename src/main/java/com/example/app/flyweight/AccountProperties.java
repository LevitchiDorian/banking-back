package com.example.app.flyweight;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AccountProperties {
    // Getters
    private final BigDecimal overdraftLimit;
    private final BigDecimal interestRate;

    public AccountProperties(BigDecimal overdraftLimit, BigDecimal interestRate) {
        this.overdraftLimit = overdraftLimit;
        this.interestRate = interestRate;
    }

}