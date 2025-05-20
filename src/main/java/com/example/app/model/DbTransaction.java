package com.example.app.model;

import jakarta.persistence.*;
import lombok.Data; // Continuăm să folosim Lombok dacă așa era
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "db_transactions") // Numele tabelului tău existent
@Data // Generează getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class DbTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Sau GenerationType.SEQUENCE dacă folosești secvențe
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id") // Poate fi null pentru depuneri
    private DbAccount fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")   // Poate fi null pentru retrageri
    private DbAccount toAccount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3) // Adăugăm coloana pentru monedă, ex: "LEI", "USD", "EUR"
    private String currency; // Noul câmp pentru moneda tranzacției

    @Column(length = 255) // Lungime mai mare pentru descriere, dacă e necesar
    private String description;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime timestamp = LocalDateTime.now(); // Valoare default la creare

    @Column(name = "transaction_type", length = 50)
    private String transactionType; // Ex: "OWN_ACCOUNT_TRANSFER", "INTRABANK_TRANSFER", "DEPOSIT", etc.
}