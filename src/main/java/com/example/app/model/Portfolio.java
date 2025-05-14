package com.example.app.model;

import com.example.app.interfaces.AccountComponent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Portfolio implements AccountComponent {
    private final List<AccountComponent> accounts = new ArrayList<>();

    @Override
    public BigDecimal getBalance() {
        return accounts.stream()
                .map(AccountComponent::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void add(AccountComponent component) {
        accounts.add(component);
    }

    @Override
    public void remove(AccountComponent component) {
        accounts.remove(component);
    }

    public List<AccountComponent> getAccounts() {
        return new ArrayList<>(accounts);
    }
}