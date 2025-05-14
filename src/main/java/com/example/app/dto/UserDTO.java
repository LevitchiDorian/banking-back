package com.example.app.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class UserDTO {
    private BigInteger Id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
}