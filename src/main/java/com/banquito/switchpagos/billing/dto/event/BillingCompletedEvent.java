package com.banquito.switchpagos.billing.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BillingCompletedEvent {

    private UUID eventId;
    private String eventType;
    private OffsetDateTime occurredAt;
    private UUID batchId;
    private UUID correlationId;
    private String sourceService;
    private UUID billingId;
    private Integer billableLines;
    private BigDecimal unitFee;
    private BigDecimal commissionSubtotal;
    private String currency;
    private String billingStatus;
    private String coreCommissionChargeId;
    private BigDecimal taxAmount;
    private BigDecimal totalChargedAmount;
    private BigDecimal remainingAmount;
    private String fundingAdjustmentStatus;
    private BigDecimal releasedAmount;
    private String fundingReleaseCoreTransactionId;

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public UUID getBillingId() { return billingId; }
    public void setBillingId(UUID billingId) { this.billingId = billingId; }
    public Integer getBillableLines() { return billableLines; }
    public void setBillableLines(Integer billableLines) { this.billableLines = billableLines; }
    public BigDecimal getUnitFee() { return unitFee; }
    public void setUnitFee(BigDecimal unitFee) { this.unitFee = unitFee; }
    public BigDecimal getCommissionSubtotal() { return commissionSubtotal; }
    public void setCommissionSubtotal(BigDecimal commissionSubtotal) { this.commissionSubtotal = commissionSubtotal; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getBillingStatus() { return billingStatus; }
    public void setBillingStatus(String billingStatus) { this.billingStatus = billingStatus; }
    public String getCoreCommissionChargeId() { return coreCommissionChargeId; }
    public void setCoreCommissionChargeId(String coreCommissionChargeId) { this.coreCommissionChargeId = coreCommissionChargeId; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalChargedAmount() { return totalChargedAmount; }
    public void setTotalChargedAmount(BigDecimal totalChargedAmount) { this.totalChargedAmount = totalChargedAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
    public String getFundingAdjustmentStatus() { return fundingAdjustmentStatus; }
    public void setFundingAdjustmentStatus(String fundingAdjustmentStatus) { this.fundingAdjustmentStatus = fundingAdjustmentStatus; }
    public BigDecimal getReleasedAmount() { return releasedAmount; }
    public void setReleasedAmount(BigDecimal releasedAmount) { this.releasedAmount = releasedAmount; }
    public String getFundingReleaseCoreTransactionId() { return fundingReleaseCoreTransactionId; }
    public void setFundingReleaseCoreTransactionId(String fundingReleaseCoreTransactionId) { this.fundingReleaseCoreTransactionId = fundingReleaseCoreTransactionId; }
}
