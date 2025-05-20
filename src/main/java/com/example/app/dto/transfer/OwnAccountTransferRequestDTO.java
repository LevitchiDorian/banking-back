package com.example.app.dto.transfer;

import jakarta.validation.constraints.DecimalMin; // Import nou
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive; // Poți păstra @Positive dacă vrei o verificare suplimentară (deși @DecimalMin("0.01") o implică)
import java.math.BigDecimal;

public class OwnAccountTransferRequestDTO {

    @NotNull(message = "ID-ul contului sursă este obligatoriu")
    private Long fromAccountId;

    @NotNull(message = "ID-ul contului destinație este obligatoriu")
    private Long toAccountId;

    @NotNull(message = "Suma este obligatorie")
    // @Positive(message = "Suma trebuie să fie pozitivă") // @DecimalMin("0.01") include această verificare
    @DecimalMin(value = "0.01", message = "Suma minimă pentru transfer este 0.01")
    private BigDecimal amount;

    private String description;

    // Getters and Setters
    public Long getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}