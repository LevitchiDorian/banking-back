package com.example.app.model;

import lombok.Getter;

@Getter
public class BankCustomer {
    // Getters
    private final String name;
    private final String address;
    private final String email;
    private final String phone;

    private BankCustomer(Builder builder) {
        this.name = builder.name;
        this.address = builder.address;
        this.email = builder.email;
        this.phone = builder.phone;
    }

    public static class Builder {
        private String name;
        private String address;
        private String email;
        @Getter
        private String phone;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public BankCustomer build() {
            return new BankCustomer(this);
        }

    }
}