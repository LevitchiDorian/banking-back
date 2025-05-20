package com.example.app.controller;

import com.example.app.dto.transfer.DomesticBankTransferRequestDTO;
import com.example.app.dto.transfer.IntrabankTransferRequestDTO;
import com.example.app.dto.transfer.OwnAccountTransferRequestDTO;
import com.example.app.dto.transfer.TransferResponseDTO;
import com.example.app.service.TransferService;
import com.example.app.util.UserExtractServiceImpl; // Pentru a obține utilizatorul curent
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers") // Punct de bază pentru toate transferurile
public class TransferController {

    private final TransferService transferService;
    private final UserExtractServiceImpl userExtractService; // Presupunând că ai acest serviciu

    @Autowired
    public TransferController(TransferService transferService, UserExtractServiceImpl userExtractService) {
        this.transferService = transferService;
        this.userExtractService = userExtractService;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Ar trebui să fie prins de Spring Security înainte de a ajunge aici dacă endpoint-ul e protejat
            throw new IllegalStateException("Utilizatorul nu este autentificat.");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getName(); // Fallback
    }

    @PostMapping("/own-account")
    public ResponseEntity<TransferResponseDTO> transferBetweenOwnAccounts(
            @Valid @RequestBody OwnAccountTransferRequestDTO requestDTO) {
        String username = getCurrentUsername();
        TransferResponseDTO response = transferService.transferBetweenOwnAccounts(username, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/intrabank")
    public ResponseEntity<TransferResponseDTO> transferToIntrabankAccount(
            @Valid @RequestBody IntrabankTransferRequestDTO requestDTO) {
        String username = getCurrentUsername();
        TransferResponseDTO response = transferService.transferToIntrabankAccount(username, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/domestic-bank")
    public ResponseEntity<TransferResponseDTO> transferToDomesticBankAccount(
            @Valid @RequestBody DomesticBankTransferRequestDTO requestDTO) {
        String username = getCurrentUsername();
        TransferResponseDTO response = transferService.transferToDomesticBankAccount(username, requestDTO);
        return ResponseEntity.ok(response);
    }
}