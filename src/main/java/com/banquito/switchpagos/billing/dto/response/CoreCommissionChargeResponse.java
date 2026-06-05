package com.banquito.switchpagos.billing.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class CoreCommissionChargeResponse {

    private UUID batchId;
    private String status;
    private String coreCommissionChargeId;
    private BigDecimal commissionSubtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalChargedAmount;
    private String coreTransactionId;
    private String message;

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoreCommissionChargeId() {
        return coreCommissionChargeId;
    }

    public void setCoreCommissionChargeId(String coreCommissionChargeId) {
        this.coreCommissionChargeId = coreCommissionChargeId;
    }

    public BigDecimal getCommissionSubtotal() {
        return commissionSubtotal;
    }

    public void setCommissionSubtotal(BigDecimal commissionSubtotal) {
        this.commissionSubtotal = commissionSubtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalChargedAmount() {
        return totalChargedAmount;
    }

    public void setTotalChargedAmount(BigDecimal totalChargedAmount) {
        this.totalChargedAmount = totalChargedAmount;
    }

    public String getCoreTransactionId() {
        return coreTransactionId;
    }

    public void setCoreTransactionId(String coreTransactionId) {
        this.coreTransactionId = coreTransactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
