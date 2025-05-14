package com.example.app.util;

import com.example.app.model.User;
import com.example.app.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserExtractServiceImpl {

    private final JwtServiceImpl jwtService;
    private final UserRepository userRepository;

    public User getUser(String jwtToken) {
        String username = jwtService.extractUsername(jwtToken);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No user found"));
    }
}