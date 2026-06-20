package com.banquito.switchpagos.billing.dto.response;

import java.math.BigDecimal;

public class CoreReservationResponse {

    private String reservationUuid;
    private String batchId;
    private String status;
    private BigDecimal reservedAmount;
    private BigDecimal consumedOnUs;
    private BigDecimal consumedOffUs;
    private BigDecimal releasedAmount;
    private String mainAccountNumber;
    private BigDecimal taxAmount;
    private BigDecimal totalChargedAmount;

    public String reservationUuid() { return reservationUuid; }
    public String batchId() { return batchId; }
    public String status() { return status; }
    public BigDecimal reservedAmount() { return reservedAmount; }
    public BigDecimal consumedOnUs() { return consumedOnUs; }
    public BigDecimal consumedOffUs() { return consumedOffUs; }
    public BigDecimal releasedAmount() { return releasedAmount; }
    public String mainAccountNumber() { return mainAccountNumber; }
    public BigDecimal taxAmount() { return taxAmount; }
    public BigDecimal totalChargedAmount() { return totalChargedAmount; }

    public void setReservationUuid(String reservationUuid) { this.reservationUuid = reservationUuid; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public void setStatus(String status) { this.status = status; }
    public void setReservedAmount(BigDecimal reservedAmount) { this.reservedAmount = reservedAmount; }
    public void setConsumedOnUs(BigDecimal consumedOnUs) { this.consumedOnUs = consumedOnUs; }
    public void setConsumedOffUs(BigDecimal consumedOffUs) { this.consumedOffUs = consumedOffUs; }
    public void setReleasedAmount(BigDecimal releasedAmount) { this.releasedAmount = releasedAmount; }
    public void setMainAccountNumber(String mainAccountNumber) { this.mainAccountNumber = mainAccountNumber; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public void setTotalChargedAmount(BigDecimal totalChargedAmount) { this.totalChargedAmount = totalChargedAmount; }
}
