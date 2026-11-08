package com.ledgerline.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledgerline.backend.dto.account.AccountResponse;
import com.ledgerline.backend.model.entity.Account;
import com.ledgerline.backend.model.entity.AuditLog;
import com.ledgerline.backend.model.enums.AccountStatus;
import com.ledgerline.backend.repository.AccountRepository;
import com.ledgerline.backend.repository.AuditLogRepository;

@RestController
@RequestMapping("/api/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final AccountRepository accountRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminAccountController(AccountRepository accountRepository, AuditLogRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PutMapping("/{accountNumber}/freeze")
    @Transactional
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber, Authentication auth) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.FROZEN);
        Account updated = accountRepository.save(account);

        auditLogRepository.save(new AuditLog(
                auth.getName(),
                "ACCOUNT_FREEZE",
                "Account",
                account.getId().toString(),
                null,
                "Admin froze account " + accountNumber
        ));

        return ResponseEntity.ok(new AccountResponse(
                updated.getId(),
                updated.getAccountNumber(),
                updated.getBalance(),
                updated.getCurrency(),
                updated.getStatus().name(),
                updated.getCreatedAt()
        ));
    }

    @PutMapping("/{accountNumber}/unfreeze")
    @Transactional
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountNumber, Authentication auth) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.ACTIVE);
        Account updated = accountRepository.save(account);

        auditLogRepository.save(new AuditLog(
                auth.getName(),
                "ACCOUNT_UNFREEZE",
                "Account",
                account.getId().toString(),
                null,
                "Admin unfroze account " + accountNumber
        ));

        return ResponseEntity.ok(new AccountResponse(
                updated.getId(),
                updated.getAccountNumber(),
                updated.getBalance(),
                updated.getCurrency(),
                updated.getStatus().name(),
                updated.getCreatedAt()
        ));
    }
}