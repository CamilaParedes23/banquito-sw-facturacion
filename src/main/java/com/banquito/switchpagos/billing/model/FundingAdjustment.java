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
@Table(name = "\"AJUSTE_FONDEO\"")
public class FundingAdjustment {

    @Id
    @Column(name = "\"ID_AJUSTE_FONDEO\"", nullable = false)
    private UUID fundingAdjustmentId;

    @Column(name = "\"ID_COBRO_COMISION\"", nullable = false)
    private UUID billingId;

    @Column(name = "\"ID_LOTE\"", nullable = false, unique = true)
    private UUID batchId;

    @Column(name = "\"ID_FONDEO_CORE\"")
    private String coreFundingId;

    @Column(name = "\"MONTO_REMANENTE\"", nullable = false)
    private BigDecimal remainingAmount;

    @Column(name = "\"MONTO_LIBERADO\"")
    private BigDecimal releasedAmount;

    @Column(name = "\"MONEDA\"", nullable = false)
    private String currency;

    @Column(name = "\"ESTADO\"", nullable = false)
    private String status;

    @Column(name = "\"ID_TRANSACCION_CORE\"")
    private String coreTransactionId;

    @Column(name = "\"ESTADO_RESPUESTA_CORE\"")
    private String coreResponseStatus;

    @Column(name = "\"MENSAJE_RESPUESTA_CORE\"")
    private String coreResponseMessage;

    @Column(name = "\"CLAVE_IDEMPOTENCIA\"", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "\"FECHA_SOLICITUD\"")
    private OffsetDateTime requestedAt;

    @Column(name = "\"FECHA_FINALIZACION\"")
    private OffsetDateTime completedAt;

    @Column(name = "\"FECHA_CREACION\"", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "\"FECHA_ACTUALIZACION\"", nullable = false)
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
