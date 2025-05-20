package com.example.app.dto.transfer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class DomesticBankTransferRequestDTO {

    @NotNull(message = "ID-ul contului sursă este obligatoriu")
    private Long fromAccountId;

    @NotBlank(message = "IBAN-ul destinație este obligatoriu")
    @Size(min = 10, max = 34, message = "IBAN-ul trebuie să aibă între 10 și 34 de caractere")
    private String toIban;

    @NotBlank(message = "Numele beneficiarului este obligatoriu")
    private String beneficiaryName;

    @NotBlank(message = "Numele băncii beneficiare este obligatoriu")
    private String beneficiaryBankName;

    @NotNull(message = "Suma este obligatorie")
    // @Positive(message = "Suma trebuie să fie pozitivă")
    @DecimalMin(value = "0.01", message = "Suma minimă pentru transfer este 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Moneda este obligatorie, de obicei LEI pentru transferuri naționale")
    @Size(min = 3, max = 3, message = "Moneda trebuie să aibă 3 caractere")
    private String currency;

    @NotBlank(message = "Descrierea/detaliile plății sunt obligatorii")
    private String description;

    // Getters and Setters
    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
    public String getToIban() { return toIban; }
    public void setToIban(String toIban) { this.toIban = toIban; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
    public String getBeneficiaryBankName() { return beneficiaryBankName; }
    public void setBeneficiaryBankName(String beneficiaryBankName) { this.beneficiaryBankName = beneficiaryBankName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}