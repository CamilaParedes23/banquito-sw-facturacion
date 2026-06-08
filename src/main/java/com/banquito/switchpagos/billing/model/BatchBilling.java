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
@Table(name = "batch_billing")
public class BatchBilling {

    @Id
    @Column(name = "billing_id", nullable = false)
    private UUID billingId;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Column(name = "batch_id", nullable = false, unique = true)
    private UUID batchId;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "company_ruc")
    private String companyRuc;

    @Column(name = "source_account_number")
    private String sourceAccountNumber;

    @Column(name = "core_funding_id")
    private String coreFundingId;

    @Column(name = "total_lines", nullable = false)
    private Integer totalLines;

    @Column(name = "on_us_credited_lines", nullable = false)
    private Integer onUsCreditedLines;

    @Column(name = "off_us_included_lines", nullable = false)
    private Integer offUsIncludedLines;

    @Column(name = "rejected_lines", nullable = false)
    private Integer rejectedLines;

    @Column(name = "failed_lines", nullable = false)
    private Integer failedLines;

    @Column(name = "billable_lines", nullable = false)
    private Integer billableLines;

    @Column(name = "control_amount")
    private BigDecimal controlAmount;

    @Column(name = "processed_amount", nullable = false)
    private BigDecimal processedAmount;

    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "unit_fee", nullable = false)
    private BigDecimal unitFee;

    @Column(name = "commission_subtotal", nullable = false)
    private BigDecimal commissionSubtotal;

    @Column(name = "status", nullable = false)
    private String status;

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

    @Column(name = "billing_completed_event_published", nullable = false)
    private Boolean billingCompletedEventPublished;

    @Column(name = "billing_completed_event_id")
    private UUID billingCompletedEventId;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "calculated_at")
    private OffsetDateTime calculatedAt;

    @Column(name = "core_requested_at")
    private OffsetDateTime coreRequestedAt;

    @Column(name = "core_responded_at")
    private OffsetDateTime coreRespondedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public BatchBilling() {
    }

    public BatchBilling(UUID billingId) {
        this.billingId = billingId;
    }

    public UUID getBillingId() { return billingId; }
    public void setBillingId(UUID billingId) { this.billingId = billingId; }
    public UUID getSourceEventId() { return sourceEventId; }
    public void setSourceEventId(UUID sourceEventId) { this.sourceEventId = sourceEventId; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }
    public String getCompanyRuc() { return companyRuc; }
    public void setCompanyRuc(String companyRuc) { this.companyRuc = companyRuc; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }
    public String getCoreFundingId() { return coreFundingId; }
    public void setCoreFundingId(String coreFundingId) { this.coreFundingId = coreFundingId; }
    public Integer getTotalLines() { return totalLines; }
    public void setTotalLines(Integer totalLines) { this.totalLines = totalLines; }
    public Integer getOnUsCreditedLines() { return onUsCreditedLines; }
    public void setOnUsCreditedLines(Integer onUsCreditedLines) { this.onUsCreditedLines = onUsCreditedLines; }
    public Integer getOffUsIncludedLines() { return offUsIncludedLines; }
    public void setOffUsIncludedLines(Integer offUsIncludedLines) { this.offUsIncludedLines = offUsIncludedLines; }
    public Integer getRejectedLines() { return rejectedLines; }
    public void setRejectedLines(Integer rejectedLines) { this.rejectedLines = rejectedLines; }
    public Integer getFailedLines() { return failedLines; }
    public void setFailedLines(Integer failedLines) { this.failedLines = failedLines; }
    public Integer getBillableLines() { return billableLines; }
    public void setBillableLines(Integer billableLines) { this.billableLines = billableLines; }
    public BigDecimal getControlAmount() { return controlAmount; }
    public void setControlAmount(BigDecimal controlAmount) { this.controlAmount = controlAmount; }
    public BigDecimal getProcessedAmount() { return processedAmount; }
    public void setProcessedAmount(BigDecimal processedAmount) { this.processedAmount = processedAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getUnitFee() { return unitFee; }
    public void setUnitFee(BigDecimal unitFee) { this.unitFee = unitFee; }
    public BigDecimal getCommissionSubtotal() { return commissionSubtotal; }
    public void setCommissionSubtotal(BigDecimal commissionSubtotal) { this.commissionSubtotal = commissionSubtotal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
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
    public Boolean getBillingCompletedEventPublished() { return billingCompletedEventPublished; }
    public void setBillingCompletedEventPublished(Boolean billingCompletedEventPublished) { this.billingCompletedEventPublished = billingCompletedEventPublished; }
    public UUID getBillingCompletedEventId() { return billingCompletedEventId; }
    public void setBillingCompletedEventId(UUID billingCompletedEventId) { this.billingCompletedEventId = billingCompletedEventId; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(OffsetDateTime receivedAt) { this.receivedAt = receivedAt; }
    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public OffsetDateTime getCoreRequestedAt() { return coreRequestedAt; }
    public void setCoreRequestedAt(OffsetDateTime coreRequestedAt) { this.coreRequestedAt = coreRequestedAt; }
    public OffsetDateTime getCoreRespondedAt() { return coreRespondedAt; }
    public void setCoreRespondedAt(OffsetDateTime coreRespondedAt) { this.coreRespondedAt = coreRespondedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof BatchBilling that)) { return false; }
        return Objects.equals(billingId, that.billingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billingId);
    }

    @Override
    public String toString() {
        return "BatchBilling{billingId=" + billingId + ", batchId=" + batchId + ", status=" + status + "}";
    }
}
