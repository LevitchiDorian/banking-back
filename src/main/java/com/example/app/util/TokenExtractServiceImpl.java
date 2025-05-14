package com.example.app.util;

import org.springframework.stereotype.Service;

public class TokenExtractServiceImpl {

    private static TokenExtractServiceImpl instance;
    private TokenExtractServiceImpl() {
    }

    public static TokenExtractServiceImpl getInstance() {
        if (instance == null) {
            instance = new TokenExtractServiceImpl();
        }
        return instance;
    }

    public String getToken(String token){
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}