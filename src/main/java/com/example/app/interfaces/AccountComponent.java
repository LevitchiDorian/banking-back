package com.example.app.interfaces;

import java.math.BigDecimal;

public interface AccountComponent {
    BigDecimal getBalance();
    void add(AccountComponent component);
    void remove(AccountComponent component);
}