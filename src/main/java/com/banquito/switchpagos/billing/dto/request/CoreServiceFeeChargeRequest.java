package com.banquito.switchpagos.billing.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public class CoreServiceFeeChargeRequest {

    private BigDecimal commissionSubtotal;
    private UUID correlationId;
    private String externalReference;

    public BigDecimal getCommissionSubtotal() {
        return commissionSubtotal;
    }

    public void setCommissionSubtotal(BigDecimal commissionSubtotal) {
        this.commissionSubtotal = commissionSubtotal;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }
}
