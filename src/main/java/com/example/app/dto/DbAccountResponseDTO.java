package com.example.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DbAccountResponseDTO {
    private Long id;
    private String accountNumber;
    private String userName;
    private String accountTypeName;
    private BigDecimal balance;
    private String currency;
    private BigDecimal insuranceBenefit;
    private Boolean hasPremiumBenefits;
    private LocalDateTime openedDate;

    // Constructors, Getters, and Setters

    public DbAccountResponseDTO() {
    }

    public DbAccountResponseDTO(Long id, String accountNumber, String userName, String accountTypeName, BigDecimal balance, String currency, BigDecimal insuranceBenefit, Boolean hasPremiumBenefits, LocalDateTime openedDate) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.userName = userName;
        this.accountTypeName = accountTypeName;
        this.balance = balance;
        this.currency = currency;
        this.insuranceBenefit = insuranceBenefit;
        this.hasPremiumBenefits = hasPremiumBenefits;
        this.openedDate = openedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public void setAccountTypeName(String accountTypeName) {
        this.accountTypeName = accountTypeName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getInsuranceBenefit() {
        return insuranceBenefit;
    }

    public void setInsuranceBenefit(BigDecimal insuranceBenefit) {
        this.insuranceBenefit = insuranceBenefit;
    }

    public Boolean getHasPremiumBenefits() {
        return hasPremiumBenefits;
    }

    public void setHasPremiumBenefits(Boolean hasPremiumBenefits) {
        this.hasPremiumBenefits = hasPremiumBenefits;
    }

    public LocalDateTime getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(LocalDateTime openedDate) {
        this.openedDate = openedDate;
    }
}