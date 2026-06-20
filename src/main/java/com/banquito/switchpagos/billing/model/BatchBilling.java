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
@Table(name = "\"COBRO_COMISION_LOTE\"")
public class BatchBilling {

    @Id
    @Column(name = "\"ID_COBRO_COMISION\"", nullable = false)
    private UUID billingId;

    @Column(name = "\"ID_EVENTO_ORIGEN\"", nullable = false)
    private UUID sourceEventId;

    @Column(name = "\"ID_LOTE\"", nullable = false, unique = true)
    private UUID batchId;

    @Column(name = "\"ID_CORRELACION\"", nullable = false)
    private UUID correlationId;

    @Column(name = "\"RUC_EMPRESA\"")
    private String companyRuc;

    @Column(name = "\"NUMERO_CUENTA_MATRIZ\"")
    private String sourceAccountNumber;

    @Column(name = "\"ID_FONDEO_CORE\"")
    private String coreFundingId;

    @Column(name = "\"TOTAL_LINEAS\"", nullable = false)
    private Integer totalLines;

    @Column(name = "\"LINEAS_ON_US_ACREDITADAS\"", nullable = false)
    private Integer onUsCreditedLines;

    @Column(name = "\"LINEAS_OFF_US_INCLUIDAS\"", nullable = false)
    private Integer offUsIncludedLines;

    @Column(name = "\"LINEAS_RECHAZADAS\"", nullable = false)
    private Integer rejectedLines;

    @Column(name = "\"LINEAS_FALLIDAS\"", nullable = false)
    private Integer failedLines;

    @Column(name = "\"LINEAS_COBRABLES\"", nullable = false)
    private Integer billableLines;

    @Column(name = "\"MONTO_CONTROL\"")
    private BigDecimal controlAmount;

    @Column(name = "\"MONTO_PROCESADO\"", nullable = false)
    private BigDecimal processedAmount;

    @Column(name = "\"MONTO_REMANENTE\"")
    private BigDecimal remainingAmount;

    @Column(name = "\"MONEDA\"", nullable = false)
    private String currency;

    @Column(name = "\"TARIFA_UNITARIA\"", nullable = false)
    private BigDecimal unitFee;

    @Column(name = "\"SUBTOTAL_COMISION\"", nullable = false)
    private BigDecimal commissionSubtotal;

    @Column(name = "\"ESTADO\"", nullable = false)
    private String status;

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

    @Column(name = "\"EVENTO_COBRO_COMPLETADO_PUBLICADO\"", nullable = false)
    private Boolean billingCompletedEventPublished;

    @Column(name = "\"ID_EVENTO_COBRO_COMPLETADO\"")
    private UUID billingCompletedEventId;

    @Column(name = "\"FECHA_RECEPCION\"", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "\"FECHA_CALCULO\"")
    private OffsetDateTime calculatedAt;

    @Column(name = "\"FECHA_SOLICITUD_CORE\"")
    private OffsetDateTime coreRequestedAt;

    @Column(name = "\"FECHA_RESPUESTA_CORE\"")
    private OffsetDateTime coreRespondedAt;

    @Column(name = "\"FECHA_FINALIZACION\"")
    private OffsetDateTime completedAt;

    @Column(name = "\"FECHA_ACTUALIZACION\"", nullable = false)
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
