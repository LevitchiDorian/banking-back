package com.example.app.service;

public class ThirdPartySMS {
    public void sendSMS(String message, String phoneNumber) {
        System.out.println("Trimite SMS către " + phoneNumber + ": " + message);
    }
}