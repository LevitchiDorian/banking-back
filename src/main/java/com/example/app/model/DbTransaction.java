package com.example.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "db_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private DbAccount fromAccount; // Nullable for deposits from external sources

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private DbAccount toAccount;   // Nullable for withdrawals to external entities

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column
    private String description;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "transaction_type", length = 50)
    private String transactionType; // e.g., "TRANSFER", "DEPOSIT", "WITHDRAWAL"
}