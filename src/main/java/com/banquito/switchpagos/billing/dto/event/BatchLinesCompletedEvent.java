package com.banquito.switchpagos.billing.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BatchLinesCompletedEvent {

    private UUID eventId;
    private String eventType;
    private OffsetDateTime occurredAt;
    private UUID batchId;
    private UUID correlationId;
    private String sourceService;
    private String companyRuc;
    private String sourceAccountNumber;
    private String coreFundingId;
    private Integer totalLines;
    private Integer onUsCreditedLines;
    private Integer offUsIncludedLines;
    private Integer rejectedLines;
    private Integer failedLines;
    private Integer billableLines;
    private BigDecimal controlAmount;
    private BigDecimal processedAmount;
    private BigDecimal remainingAmount;
    private String currency;

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
}
