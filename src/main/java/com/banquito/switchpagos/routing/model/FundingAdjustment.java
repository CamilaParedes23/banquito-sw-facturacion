package com.banquito.switchpagos.billing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "funding_adjustment")
public class FundingAdjustment {

    @Id
    @Column(name = "funding_adjustment_id", nullable = false)
    private UUID fundingAdjustmentId;

    @Column(name = "billing_id", nullable = false)
    private UUID billingId;

    @Column(name = "batch_id", nullable = false, unique = true)
    private UUID batchId;

    @Column(name = "core_funding_id")
    private String coreFundingId;

    @Column(name = "remaining_amount", nullable = false)
    private BigDecimal remainingAmount;

    @Column(name = "released_amount")
    private BigDecimal releasedAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "core_transaction_id")
    private String coreTransactionId;

    @Column(name = "core_response_status")
    private String coreResponseStatus;

    @Column(name = "core_response_message")
    private String coreResponseMessage;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "requested_at")
    private OffsetDateTime requestedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public FundingAdjustment() {
    }

    public FundingAdjustment(UUID fundingAdjustmentId) {
        this.fundingAdjustmentId = fundingAdjustmentId;
    }

    public UUID getFundingAdjustmentId() { return fundingAdjustmentId; }
    public void setFundingAdjustmentId(UUID fundingAdjustmentId) { this.fundingAdjustmentId = fundingAdjustmentId; }
    public UUID getBillingId() { return billingId; }
    public void setBillingId(UUID billingId) { this.billingId = billingId; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public String getCoreFundingId() { return coreFundingId; }
    public void setCoreFundingId(String coreFundingId) { this.coreFundingId = coreFundingId; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
    public BigDecimal getReleasedAmount() { return releasedAmount; }
    public void setReleasedAmount(BigDecimal releasedAmount) { this.releasedAmount = releasedAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCoreTransactionId() { return coreTransactionId; }
    public void setCoreTransactionId(String coreTransactionId) { this.coreTransactionId = coreTransactionId; }
    public String getCoreResponseStatus() { return coreResponseStatus; }
    public void setCoreResponseStatus(String coreResponseStatus) { this.coreResponseStatus = coreResponseStatus; }
    public String getCoreResponseMessage() { return coreResponseMessage; }
    public void setCoreResponseMessage(String coreResponseMessage) { this.coreResponseMessage = coreResponseMessage; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(OffsetDateTime requestedAt) { this.requestedAt = requestedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof FundingAdjustment that)) { return false; }
        return Objects.equals(fundingAdjustmentId, that.fundingAdjustmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fundingAdjustmentId);
    }

    @Override
    public String toString() {
        return "FundingAdjustment{fundingAdjustmentId=" + fundingAdjustmentId
                + ", batchId=" + batchId + ", status=" + status + "}";
    }
}
