package com.banquito.switchpagos.billing.mapper;

import com.banquito.switchpagos.billing.dto.event.BatchLinesCompletedEvent;
import com.banquito.switchpagos.billing.dto.event.BillingCompletedEvent;
import com.banquito.switchpagos.billing.model.BatchBilling;
import com.banquito.switchpagos.billing.model.BillingCoreRequest;
import com.banquito.switchpagos.billing.model.CommissionCalculation;
import com.banquito.switchpagos.billing.model.FundingAdjustment;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BillingMapper {

    private static final String SOURCE_SERVICE = "banquito-switch-billing-service";

    public BatchBilling toBatchBilling(
            BatchLinesCompletedEvent event,
            UUID billingId,
            String companyRuc,
            String sourceAccountNumber,
            BigDecimal unitFee,
            BigDecimal commissionSubtotal,
            String status,
            OffsetDateTime now) {
        BatchBilling billing = new BatchBilling();
        billing.setBillingId(billingId);
        billing.setSourceEventId(event.getEventId());
        billing.setBatchId(event.getBatchId());
        billing.setCorrelationId(event.getCorrelationId());
        billing.setCompanyRuc(companyRuc);
        billing.setSourceAccountNumber(sourceAccountNumber);
        billing.setCoreFundingId(event.getCoreFundingId());
        billing.setTotalLines(numberOrZero(event.getTotalLines()));
        billing.setOnUsCreditedLines(numberOrZero(event.getOnUsCreditedLines()));
        billing.setOffUsIncludedLines(numberOrZero(event.getOffUsIncludedLines()));
        billing.setRejectedLines(numberOrZero(event.getRejectedLines()));
        billing.setFailedLines(numberOrZero(event.getFailedLines()));
        billing.setBillableLines(numberOrZero(event.getBillableLines()));
        billing.setControlAmount(event.getControlAmount());
        billing.setProcessedAmount(amountOrZero(event.getProcessedAmount()));
        billing.setRemainingAmount(event.getRemainingAmount());
        billing.setCurrency(currencyOrDefault(event.getCurrency()));
        billing.setUnitFee(unitFee);
        billing.setCommissionSubtotal(commissionSubtotal);
        billing.setStatus(status);
        billing.setBillingCompletedEventPublished(false);
        billing.setReceivedAt(now);
        billing.setCalculatedAt(now);
        billing.setUpdatedAt(now);
        return billing;
    }

    public CommissionCalculation toCommissionCalculation(BatchBilling billing, OffsetDateTime now) {
        CommissionCalculation calculation = new CommissionCalculation();
        calculation.setCommissionCalculationId(UUID.randomUUID());
        calculation.setBillingId(billing.getBillingId());
        calculation.setBatchId(billing.getBatchId());
        calculation.setBillableLines(billing.getBillableLines());
        calculation.setUnitFee(billing.getUnitFee());
        calculation.setCommissionSubtotal(billing.getCommissionSubtotal());
        calculation.setCurrency(billing.getCurrency());
        calculation.setCalculatedAt(now);
        return calculation;
    }

    public BillingCoreRequest toBillingCoreRequest(BatchBilling billing, String idempotencyKey, OffsetDateTime now) {
        BillingCoreRequest request = new BillingCoreRequest();
        request.setBillingCoreRequestId(UUID.randomUUID());
        request.setBillingId(billing.getBillingId());
        request.setBatchId(billing.getBatchId());
        request.setIdempotencyKey(idempotencyKey);
        request.setRequestStatus("SOLICITADO");
        request.setRequestedAmount(billing.getCommissionSubtotal());
        request.setCurrency(billing.getCurrency());
        request.setRequestedAt(now);
        return request;
    }

    public BillingCompletedEvent toBillingCompletedEvent(BatchBilling billing, UUID eventId, OffsetDateTime occurredAt) {
        return toBillingCompletedEvent(billing, null, eventId, occurredAt);
    }

    public BillingCompletedEvent toBillingCompletedEvent(
            BatchBilling billing,
            FundingAdjustment fundingAdjustment,
            UUID eventId,
            OffsetDateTime occurredAt) {
        BillingCompletedEvent event = new BillingCompletedEvent();
        event.setEventId(eventId);
        event.setEventType("BILLING_COMPLETED");
        event.setOccurredAt(occurredAt);
        event.setBatchId(billing.getBatchId());
        event.setCorrelationId(billing.getCorrelationId());
        event.setSourceService(SOURCE_SERVICE);
        event.setBillingId(billing.getBillingId());
        event.setBillableLines(billing.getBillableLines());
        event.setUnitFee(billing.getUnitFee());
        event.setCommissionSubtotal(billing.getCommissionSubtotal());
        event.setCurrency(billing.getCurrency());
        event.setBillingStatus(billing.getStatus());
        event.setCoreCommissionChargeId(billing.getCoreCommissionChargeId());
        event.setTaxAmount(billing.getTaxAmount());
        event.setTotalChargedAmount(billing.getTotalChargedAmount());
        event.setRemainingAmount(billing.getRemainingAmount());
        event.setFundingAdjustmentStatus(fundingAdjustment == null ? null : fundingAdjustment.getStatus());
        event.setReleasedAmount(fundingAdjustment == null ? null : fundingAdjustment.getReleasedAmount());
        event.setFundingReleaseCoreTransactionId(fundingAdjustment == null ? null : fundingAdjustment.getCoreTransactionId());
        return event;
    }

    public FundingAdjustment toFundingAdjustment(
            BatchBilling billing,
            String idempotencyKey,
            String status,
            OffsetDateTime now) {
        FundingAdjustment adjustment = new FundingAdjustment();
        adjustment.setFundingAdjustmentId(UUID.randomUUID());
        adjustment.setBillingId(billing.getBillingId());
        adjustment.setBatchId(billing.getBatchId());
        adjustment.setCoreFundingId(billing.getCoreFundingId());
        adjustment.setRemainingAmount(amountOrZero(billing.getRemainingAmount()));
        adjustment.setCurrency(billing.getCurrency());
        adjustment.setStatus(status);
        adjustment.setIdempotencyKey(idempotencyKey);
        adjustment.setCreatedAt(now);
        adjustment.setUpdatedAt(now);
        return adjustment;
    }

    private Integer numberOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal amountOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String currencyOrDefault(String value) {
        return value == null || value.isBlank() ? "USD" : value;
    }
}
