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
@Table(name = "\"SOLICITUD_COBRO_CORE\"")
public class BillingCoreRequest {

    @Id
    @Column(name = "\"ID_SOLICITUD_COBRO_CORE\"", nullable = false)
    private UUID billingCoreRequestId;

    @Column(name = "\"ID_COBRO_COMISION\"", nullable = false)
    private UUID billingId;

    @Column(name = "\"ID_LOTE\"", nullable = false, unique = true)
    private UUID batchId;

    @Column(name = "\"CLAVE_IDEMPOTENCIA\"", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "\"ESTADO_SOLICITUD\"", nullable = false)
    private String requestStatus;

    @Column(name = "\"MONTO_SOLICITADO\"", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "\"MONEDA\"", nullable = false)
    private String currency;

    @Column(name = "\"ESTADO_RESPUESTA_CORE\"")
    private String coreResponseStatus;

    @Column(name = "\"ID_COBRO_COMISION_CORE\"")
    private String coreCommissionChargeId;

    @Column(name = "\"ID_TRANSACCION_CORE\"")
    private String coreTransactionId;

    @Column(name = "\"MONTO_IMPUESTO\"")
    private BigDecimal taxAmount;

    @Column(name = "\"MONTO_TOTAL_COBRADO\"")
    private BigDecimal totalChargedAmount;

    @Column(name = "\"MENSAJE_RESPUESTA_CORE\"")
    private String coreResponseMessage;

    @Column(name = "\"FECHA_SOLICITUD\"", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "\"FECHA_RESPUESTA\"")
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
