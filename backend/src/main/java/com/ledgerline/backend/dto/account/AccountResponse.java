package com.ledgerline.backend.dto.account;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountResponse {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;
    private String status;
    private Instant createdAt;

    public AccountResponse() {}

    public AccountResponse(Long id, String accountNumber, BigDecimal balance, String currency, String status, Instant createdAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}