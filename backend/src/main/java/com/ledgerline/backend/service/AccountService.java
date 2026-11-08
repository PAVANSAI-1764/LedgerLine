package com.ledgerline.backend.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ledgerline.backend.dto.account.AccountResponse;
import com.ledgerline.backend.dto.account.CreateAccountRequest;
import com.ledgerline.backend.model.entity.Account;
import com.ledgerline.backend.model.entity.AuditLog;
import com.ledgerline.backend.model.entity.User;
import com.ledgerline.backend.model.enums.AccountStatus;
import com.ledgerline.backend.repository.AccountRepository;
import com.ledgerline.backend.repository.AuditLogRepository;
import com.ledgerline.backend.repository.UserRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AccountResponse createAccount(String username, CreateAccountRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String accountNumber = generateUniqueAccountNumber();

        Account account = new Account(
                accountNumber,
                user,
                request.getInitialDeposit(),
                request.getCurrency().toUpperCase()
        );

        Account saved = accountRepository.save(account);

        auditLogRepository.save(new AuditLog(
                username,
                "CREATE_ACCOUNT",
                "Account",
                saved.getAccountNumber(),
                null,
                "Created account with initial balance " + request.getInitialDeposit()
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String username, String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        if (!account.getUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized access to account: " + accountNumber);
        }

        return toResponse(account);
    }

    @Transactional
    public AccountResponse updateAccountStatus(String adminUsername, String accountNumber, AccountStatus newStatus) {
        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        AccountStatus previousStatus = account.getStatus();
        account.setStatus(newStatus);
        Account saved = accountRepository.save(account);

        auditLogRepository.save(new AuditLog(
                adminUsername,
                "UPDATE_STATUS",
                "Account",
                accountNumber,
                null,
                "Status changed from " + previousStatus + " to " + newStatus
        ));

        return toResponse(saved);
    }

    private String generateUniqueAccountNumber() {
        String num;
        do {
            long val = Math.abs(secureRandom.nextLong()) % 1_000_000_000_000L;
            num = String.format("LL%012d", val);
        } while (accountRepository.existsByAccountNumber(num));
        return num;
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name(),
                account.getCreatedAt()
        );
    }
}