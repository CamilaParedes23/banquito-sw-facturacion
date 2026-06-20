package com.banquito.switchpagos.billing.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public class CoreCommissionChargeRequest {

    private UUID batchId;
    private UUID correlationId;
    private String coreFundingId;
    private String companyRuc;
    private String sourceAccountNumber;
    private Integer billableLines;
    private BigDecimal commissionSubtotal;
    private String currency;
    private String concept;
    private String idempotencyKey;

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public String getCoreFundingId() {
        return coreFundingId;
    }

    public void setCoreFundingId(String coreFundingId) {
        this.coreFundingId = coreFundingId;
    }

    public String getCompanyRuc() {
        return companyRuc;
    }

    public void setCompanyRuc(String companyRuc) {
        this.companyRuc = companyRuc;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public Integer getBillableLines() {
        return billableLines;
    }

    public void setBillableLines(Integer billableLines) {
        this.billableLines = billableLines;
    }

    public BigDecimal getCommissionSubtotal() {
        return commissionSubtotal;
    }

    public void setCommissionSubtotal(BigDecimal commissionSubtotal) {
        this.commissionSubtotal = commissionSubtotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
