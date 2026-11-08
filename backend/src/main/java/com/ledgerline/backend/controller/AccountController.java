package com.ledgerline.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ledgerline.backend.dto.account.AccountResponse;
import com.ledgerline.backend.dto.account.CreateAccountRequest;
import com.ledgerline.backend.model.enums.AccountStatus;
import com.ledgerline.backend.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(Authentication authentication) {
        return ResponseEntity.ok(accountService.getUserAccounts(authentication.getName()));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            Authentication authentication,
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(authentication.getName(), accountNumber));
    }

    @PatchMapping("/{accountNumber}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> updateStatus(
            Authentication authentication,
            @PathVariable String accountNumber,
            @RequestParam AccountStatus status) {
        return ResponseEntity.ok(accountService.updateAccountStatus(authentication.getName(), accountNumber, status));
    }
}