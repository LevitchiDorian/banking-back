package com.example.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "db_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER) // Eager fetch type as it's often needed
    @JoinColumn(name = "account_type_id", nullable = false)
    private AccountType accountType;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 3, nullable = false)
    private String currency = "LEI";

    @Column(name = "insurance_benefit", precision = 15, scale = 2)
    private BigDecimal insuranceBenefit;

    @Column(name = "has_premium_benefits")
    private Boolean hasPremiumBenefits = false;

    @Column(name = "opened_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime openedDate = LocalDateTime.now();

    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DbTransaction> outgoingTransactions;

    @OneToMany(mappedBy = "toAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DbTransaction> incomingTransactions;
}