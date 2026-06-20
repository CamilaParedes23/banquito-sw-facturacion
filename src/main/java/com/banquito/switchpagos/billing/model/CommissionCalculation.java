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
@Table(name = "\"CALCULO_COMISION\"")
public class CommissionCalculation {

    @Id
    @Column(name = "\"ID_CALCULO_COMISION\"", nullable = false)
    private UUID commissionCalculationId;

    @Column(name = "\"ID_COBRO_COMISION\"", nullable = false)
    private UUID billingId;

    @Column(name = "\"ID_LOTE\"", nullable = false, unique = true)
    private UUID batchId;

    @Column(name = "\"LINEAS_COBRABLES\"", nullable = false)
    private Integer billableLines;

    @Column(name = "\"TARIFA_UNITARIA\"", nullable = false)
    private BigDecimal unitFee;

    @Column(name = "\"SUBTOTAL_COMISION\"", nullable = false)
    private BigDecimal commissionSubtotal;

    @Column(name = "\"MONEDA\"", nullable = false)
    private String currency;

    @Column(name = "\"FECHA_CALCULO\"", nullable = false)
    private OffsetDateTime calculatedAt;

    public CommissionCalculation() {
    }

    public CommissionCalculation(UUID commissionCalculationId) {
        this.commissionCalculationId = commissionCalculationId;
    }

    public UUID getCommissionCalculationId() { return commissionCalculationId; }
    public void setCommissionCalculationId(UUID commissionCalculationId) { this.commissionCalculationId = commissionCalculationId; }
    public UUID getBillingId() { return billingId; }
    public void setBillingId(UUID billingId) { this.billingId = billingId; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public Integer getBillableLines() { return billableLines; }
    public void setBillableLines(Integer billableLines) { this.billableLines = billableLines; }
    public BigDecimal getUnitFee() { return unitFee; }
    public void setUnitFee(BigDecimal unitFee) { this.unitFee = unitFee; }
    public BigDecimal getCommissionSubtotal() { return commissionSubtotal; }
    public void setCommissionSubtotal(BigDecimal commissionSubtotal) { this.commissionSubtotal = commissionSubtotal; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(OffsetDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof CommissionCalculation that)) { return false; }
        return Objects.equals(commissionCalculationId, that.commissionCalculationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commissionCalculationId);
    }

    @Override
    public String toString() {
        return "CommissionCalculation{commissionCalculationId=" + commissionCalculationId
                + ", batchId=" + batchId + ", commissionSubtotal=" + commissionSubtotal + "}";
    }
}
