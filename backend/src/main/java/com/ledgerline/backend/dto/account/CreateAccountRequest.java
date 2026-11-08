package com.ledgerline.backend.dto.account;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAccountRequest {

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.0000", message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;

    @NotBlank(message = "Currency is required")
    private String currency;

    public CreateAccountRequest() {}

    public CreateAccountRequest(BigDecimal initialDeposit, String currency) {
        this.initialDeposit = initialDeposit;
        this.currency = currency;
    }

    public BigDecimal getInitialDeposit() { return initialDeposit; }
    public void setInitialDeposit(BigDecimal initialDeposit) { this.initialDeposit = initialDeposit; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}