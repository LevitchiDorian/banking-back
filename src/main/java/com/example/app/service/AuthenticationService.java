package com.example.app.service;

import com.example.app.dto.LoginRequest;
import com.example.app.dto.RegisterRequest;
import com.example.app.dto.UserDTO;
import com.example.app.exception.UserAlreadyExistsException;
import com.example.app.model.User;
import com.example.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;



    public void signup(UserDTO userDTO) {
        var user = userRepository.findByUsername(userDTO.getUsername());
        User newUser = new User();

        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setEmail(userDTO.getEmail());


        if (userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isEmpty()) {
            newUser.setPhoneNumber("");
        } else {
            newUser.setPhoneNumber(userDTO.getPhoneNumber());
        }

        userRepository.save(newUser);
    }


    public User login(UserDTO userDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDTO.getUsername(),
                        userDTO.getPassword()
                )
        );

        return userRepository.findByUsername(userDTO.getUsername())
                .orElseThrow();
    }
}