package com.example.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CreateAccountRequestDTO {

    @NotBlank(message = "Account type name is required")
    private String accountTypeName; // e.g., "STANDARD_CHECKING", "PREMIUM_SAVINGS"

    @NotNull(message = "Initial deposit is required")
    @PositiveOrZero(message = "Initial deposit must be zero or positive")
    private BigDecimal initialDeposit;

    private String currency; // Optional, defaults to "LEI" if not provided

    // Getters and Setters
    public String getAccountTypeName() {
        return accountTypeName;
    }

    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}