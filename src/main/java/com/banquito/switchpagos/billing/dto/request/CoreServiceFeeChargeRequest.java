package com.banquito.switchpagos.billing.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CoreServiceFeeChargeRequest {

    private BigDecimal amount;
    private LocalDate accountingDate;
    private UUID correlationId;
    private String externalReference;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getAccountingDate() {
        return accountingDate;
    }

    public void setAccountingDate(LocalDate accountingDate) {
        this.accountingDate = accountingDate;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }
}
