package com.ledgerline.backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.ledgerline.backend.dto.transaction.DepositWithdrawRequest;
import com.ledgerline.backend.dto.transaction.TransactionResponse;
import com.ledgerline.backend.dto.transaction.TransferRequest;
import com.ledgerline.backend.model.entity.Account;
import com.ledgerline.backend.model.entity.AuditLog;
import com.ledgerline.backend.model.entity.LedgerEntry;
import com.ledgerline.backend.model.entity.Transaction;
import com.ledgerline.backend.model.enums.AccountStatus;
import com.ledgerline.backend.model.enums.EntryType;
import com.ledgerline.backend.model.enums.TransactionStatus;
import com.ledgerline.backend.model.enums.TransactionType;
import com.ledgerline.backend.repository.AccountRepository;
import com.ledgerline.backend.repository.AuditLogRepository;
import com.ledgerline.backend.repository.LedgerEntryRepository;
import com.ledgerline.backend.repository.TransactionRepository;
@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditLogRepository auditLogRepository;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,
            AuditLogRepository auditLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse transfer(String username, TransferRequest request) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        String firstLockNum = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0
                ? request.getSourceAccountNumber() : request.getDestinationAccountNumber();
        String secondLockNum = firstLockNum.equals(request.getSourceAccountNumber())
                ? request.getDestinationAccountNumber() : request.getSourceAccountNumber();

        Account first = accountRepository.findByAccountNumberWithLock(firstLockNum)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + firstLockNum));
        Account second = accountRepository.findByAccountNumberWithLock(secondLockNum)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + secondLockNum));

        Account source = first.getAccountNumber().equals(request.getSourceAccountNumber()) ? first : second;
        Account destination = first.getAccountNumber().equals(request.getDestinationAccountNumber()) ? first : second;

        if (!source.getUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized transfer from account: " + source.getAccountNumber());
        }

        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active");
        }

        if (destination.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Destination account is not active");
        }

        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new IllegalArgumentException("Cross-currency transfers are not supported");
        }

        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds in source account");
        }

        Transaction tx = new Transaction();
        tx.setIdempotencyKey(request.getIdempotencyKey());
        tx.setTransactionType(TransactionType.TRANSFER);
        tx.setAmount(request.getAmount());
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(request.getDescription());
        tx.setSourceAccount(source);
        tx.setDestinationAccount(destination);
        Transaction savedTx = transactionRepository.save(tx);

        source.setBalance(source.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));

        accountRepository.save(source);
        accountRepository.save(destination);

        LedgerEntry debitEntry = new LedgerEntry(savedTx, source, EntryType.DEBIT, request.getAmount());
        LedgerEntry creditEntry = new LedgerEntry(savedTx, destination, EntryType.CREDIT, request.getAmount());

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        auditLogRepository.save(new AuditLog(
                username,
                "TRANSFER",
                "Transaction",
                savedTx.getId().toString(),
                null,
                String.format("Transferred %s %s from %s to %s",
                        request.getAmount(), source.getCurrency(), source.getAccountNumber(), destination.getAccountNumber())
        ));

        return toResponse(savedTx);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse deposit(String username, DepositWithdrawRequest request) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountNumber()));

        if (!account.getUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized access to account: " + account.getAccountNumber());
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }

        Transaction tx = new Transaction();
        tx.setIdempotencyKey(request.getIdempotencyKey());
        tx.setTransactionType(TransactionType.DEPOSIT);
        tx.setAmount(request.getAmount());
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(request.getDescription());
        tx.setDestinationAccount(account);
        Transaction savedTx = transactionRepository.save(tx);

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        LedgerEntry creditEntry = new LedgerEntry(savedTx, account, EntryType.CREDIT, request.getAmount());
        ledgerEntryRepository.save(creditEntry);

        auditLogRepository.save(new AuditLog(
                username,
                "DEPOSIT",
                "Transaction",
                savedTx.getId().toString(),
                null,
                String.format("Deposited %s %s to %s", request.getAmount(), account.getCurrency(), account.getAccountNumber())
        ));

        return toResponse(savedTx);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse withdraw(String username, DepositWithdrawRequest request) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountNumber()));

        if (!account.getUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized access to account: " + account.getAccountNumber());
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient funds in account");
        }

        Transaction tx = new Transaction();
        tx.setIdempotencyKey(request.getIdempotencyKey());
        tx.setTransactionType(TransactionType.WITHDRAWAL);
        tx.setAmount(request.getAmount());
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(request.getDescription());
        tx.setSourceAccount(account);
        Transaction savedTx = transactionRepository.save(tx);

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        LedgerEntry debitEntry = new LedgerEntry(savedTx, account, EntryType.DEBIT, request.getAmount());
        ledgerEntryRepository.save(debitEntry);

        auditLogRepository.save(new AuditLog(
                username,
                "WITHDRAWAL",
                "Transaction",
                savedTx.getId().toString(),
                null,
                String.format("Withdrew %s %s from %s", request.getAmount(), account.getCurrency(), account.getAccountNumber())
        ));

        return toResponse(savedTx);
    }

    private TransactionResponse toResponse(Transaction tx) {
        String currency = null;
        if (tx.getSourceAccount() != null) {
            currency = tx.getSourceAccount().getCurrency();
        } else if (tx.getDestinationAccount() != null) {
            currency = tx.getDestinationAccount().getCurrency();
        }

        return new TransactionResponse(
                tx.getId(),
                tx.getIdempotencyKey(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                currency,
                tx.getStatus().name(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}