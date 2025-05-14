package com.example.app.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO {
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private BigDecimal overdraftLimit;
    private BigDecimal interestRate;
    private LocalDateTime openedDate;
}