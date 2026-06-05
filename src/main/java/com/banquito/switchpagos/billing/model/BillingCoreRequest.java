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
@Table(name = "billing_core_request")
public class BillingCoreRequest {

    @Id
    @Column(name = "billing_core_request_id", nullable = false)
    private UUID billingCoreRequestId;

    @Column(name = "billing_id", nullable = false)
    private UUID billingId;

    @Column(name = "batch_id", nullable = false, unique = true)
    private UUID batchId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "request_status", nullable = false)
    private String requestStatus;

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "core_response_status")
    private String coreResponseStatus;

    @Column(name = "core_commission_charge_id")
    private String coreCommissionChargeId;

    @Column(name = "core_transaction_id")
    private String coreTransactionId;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "total_charged_amount")
    private BigDecimal totalChargedAmount;

    @Column(name = "core_response_message")
    private String coreResponseMessage;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    public BillingCoreRequest() {
    }

    public BillingCoreRequest(UUID billingCoreRequestId) {
        this.billingCoreRequestId = billingCoreRequestId;
    }

    public UUID getBillingCoreRequestId() { return billingCoreRequestId; }
    public void setBillingCoreRequestId(UUID billingCoreRequestId) { this.billingCoreRequestId = billingCoreRequestId; }
    public UUID getBillingId() { return billingId; }
    public void setBillingId(UUID billingId) { this.billingId = billingId; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCoreResponseStatus() { return coreResponseStatus; }
    public void setCoreResponseStatus(String coreResponseStatus) { this.coreResponseStatus = coreResponseStatus; }
    public String getCoreCommissionChargeId() { return coreCommissionChargeId; }
    public void setCoreCommissionChargeId(String coreCommissionChargeId) { this.coreCommissionChargeId = coreCommissionChargeId; }
    public String getCoreTransactionId() { return coreTransactionId; }
    public void setCoreTransactionId(String coreTransactionId) { this.coreTransactionId = coreTransactionId; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalChargedAmount() { return totalChargedAmount; }
    public void setTotalChargedAmount(BigDecimal totalChargedAmount) { this.totalChargedAmount = totalChargedAmount; }
    public String getCoreResponseMessage() { return coreResponseMessage; }
    public void setCoreResponseMessage(String coreResponseMessage) { this.coreResponseMessage = coreResponseMessage; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(OffsetDateTime requestedAt) { this.requestedAt = requestedAt; }
    public OffsetDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(OffsetDateTime respondedAt) { this.respondedAt = respondedAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof BillingCoreRequest that)) { return false; }
        return Objects.equals(billingCoreRequestId, that.billingCoreRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billingCoreRequestId);
    }

    @Override
    public String toString() {
        return "BillingCoreRequest{billingCoreRequestId=" + billingCoreRequestId
                + ", batchId=" + batchId + ", requestStatus=" + requestStatus + "}";
    }
}
