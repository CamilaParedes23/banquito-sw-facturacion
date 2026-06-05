package com.banquito.switchpagos.billing.service.impl;

import com.banquito.switchpagos.billing.client.CoreBankingClient;
import com.banquito.switchpagos.billing.dto.event.BatchLinesCompletedEvent;
import com.banquito.switchpagos.billing.dto.event.BillingCompletedEvent;
import com.banquito.switchpagos.billing.dto.request.CoreCommissionChargeRequest;
import com.banquito.switchpagos.billing.dto.request.CoreFundingReleaseRequest;
import com.banquito.switchpagos.billing.dto.response.CoreCommissionChargeResponse;
import com.banquito.switchpagos.billing.dto.response.CoreFundingReleaseResponse;
import com.banquito.switchpagos.billing.enums.BillingStatus;
import com.banquito.switchpagos.billing.enums.CoreRequestStatus;
import com.banquito.switchpagos.billing.enums.FundingAdjustmentStatus;
import com.banquito.switchpagos.billing.exception.CoreBankingClientException;
import com.banquito.switchpagos.billing.mapper.BillingMapper;
import com.banquito.switchpagos.billing.model.BatchBilling;
import com.banquito.switchpagos.billing.model.BillingCoreRequest;
import com.banquito.switchpagos.billing.model.FundingAdjustment;
import com.banquito.switchpagos.billing.repository.BatchBillingRepository;
import com.banquito.switchpagos.billing.repository.BillingCoreRequestRepository;
import com.banquito.switchpagos.billing.repository.CommissionCalculationRepository;
import com.banquito.switchpagos.billing.repository.FundingAdjustmentRepository;
import com.banquito.switchpagos.billing.service.BillingCompletedEventPublisher;
import com.banquito.switchpagos.billing.service.BillingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingServiceImpl implements BillingService {

    private static final Logger LOG = LoggerFactory.getLogger(BillingServiceImpl.class);
    private static final String APPROVED = "APPROVED";
    private static final String RELEASED = "RELEASED";

    private final BatchBillingRepository batchBillingRepository;
    private final CommissionCalculationRepository commissionCalculationRepository;
    private final BillingCoreRequestRepository billingCoreRequestRepository;
    private final FundingAdjustmentRepository fundingAdjustmentRepository;
    private final CoreBankingClient coreBankingClient;
    private final BillingCompletedEventPublisher billingCompletedEventPublisher;
    private final BillingMapper billingMapper;
    private final BigDecimal unitFee;
    private final String fallbackCompanyRuc;
    private final String fallbackSourceAccountNumber;

    public BillingServiceImpl(
            BatchBillingRepository batchBillingRepository,
            CommissionCalculationRepository commissionCalculationRepository,
            BillingCoreRequestRepository billingCoreRequestRepository,
            FundingAdjustmentRepository fundingAdjustmentRepository,
            CoreBankingClient coreBankingClient,
            BillingCompletedEventPublisher billingCompletedEventPublisher,
            BillingMapper billingMapper,
            @Value("${switch.billing.unit-fee}") String unitFee,
            @Value("${switch.billing.fallback-company-ruc}") String fallbackCompanyRuc,
            @Value("${switch.billing.fallback-source-account-number}") String fallbackSourceAccountNumber) {
        this.batchBillingRepository = batchBillingRepository;
        this.commissionCalculationRepository = commissionCalculationRepository;
        this.billingCoreRequestRepository = billingCoreRequestRepository;
        this.fundingAdjustmentRepository = fundingAdjustmentRepository;
        this.coreBankingClient = coreBankingClient;
        this.billingCompletedEventPublisher = billingCompletedEventPublisher;
        this.billingMapper = billingMapper;
        this.unitFee = new BigDecimal(unitFee).setScale(2, RoundingMode.HALF_UP);
        this.fallbackCompanyRuc = fallbackCompanyRuc;
        this.fallbackSourceAccountNumber = fallbackSourceAccountNumber;
    }

    @Override
    @Transactional
    public synchronized void processBatchLinesCompleted(BatchLinesCompletedEvent event) {
        validateEvent(event);
        if (batchBillingRepository.findByBatchId(event.getBatchId()).isPresent()) {
            LOG.info("BatchLinesCompletedEvent duplicado ignorado para billing. batchId={}", event.getBatchId());
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Integer billableLines = event.getBillableLines() == null ? 0 : event.getBillableLines();
        BigDecimal commissionSubtotal = unitFee.multiply(new BigDecimal(billableLines)).setScale(2, RoundingMode.HALF_UP);
        String companyRuc = valueOrFallback(event.getCompanyRuc(), fallbackCompanyRuc, "companyRuc", event.getBatchId());
        String sourceAccountNumber = valueOrFallback(
                event.getSourceAccountNumber(),
                fallbackSourceAccountNumber,
                "sourceAccountNumber",
                event.getBatchId());
        UUID billingId = UUID.randomUUID();
        BatchBilling billing = billingMapper.toBatchBilling(
                event,
                billingId,
                companyRuc,
                sourceAccountNumber,
                unitFee,
                commissionSubtotal,
                BillingStatus.COMISION_CALCULADA.name(),
                now);
        batchBillingRepository.save(billing);
        FundingAdjustment fundingAdjustment = processFundingAdjustment(billing);
        if (fundingAdjustment != null && FundingAdjustmentStatus.FALLIDO.name().equals(fundingAdjustment.getStatus())) {
            billing.setStatus(BillingStatus.COBRO_COMISION_FALLIDO.name());
            billing.setUpdatedAt(OffsetDateTime.now());
            batchBillingRepository.save(billing);
            publishBillingCompleted(billing, fundingAdjustment);
            LOG.warn("Cobro de comision omitido por ajuste de sobrante fallido. batchId={}, adjustmentStatus={}",
                    event.getBatchId(), fundingAdjustment.getStatus());
            return;
        }

        commissionCalculationRepository.save(billingMapper.toCommissionCalculation(billing, OffsetDateTime.now()));

        String idempotencyKey = "COMMISSION-" + event.getBatchId();
        BillingCoreRequest coreRequestRecord = billingMapper.toBillingCoreRequest(billing, idempotencyKey, now);
        billingCoreRequestRepository.save(coreRequestRecord);
        billing.setCoreRequestedAt(now);
        batchBillingRepository.save(billing);

        try {
            CoreCommissionChargeResponse coreResponse = coreBankingClient.requestCommissionCharge(
                    toCoreRequest(billing, companyRuc, sourceAccountNumber, idempotencyKey));
            applyCoreResponse(billing, coreRequestRecord, coreResponse);
        } catch (CoreBankingClientException exception) {
            applyCoreFailure(billing, coreRequestRecord, exception);
        }

        publishBillingCompleted(billing, fundingAdjustment);
        LOG.info("Billing procesado. batchId={}, billableLines={}, commissionSubtotal={}, status={}",
                event.getBatchId(), billableLines, commissionSubtotal, billing.getStatus());
    }

    private FundingAdjustment processFundingAdjustment(BatchBilling billing) {
        if (fundingAdjustmentRepository.findByBatchId(billing.getBatchId()).isPresent()) {
            return fundingAdjustmentRepository.findByBatchId(billing.getBatchId()).get();
        }

        BigDecimal remainingAmount = billing.getRemainingAmount() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : billing.getRemainingAmount().setScale(2, RoundingMode.HALF_UP);
        String idempotencyKey = "RELEASE-" + billing.getBatchId();
        OffsetDateTime now = OffsetDateTime.now();

        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            FundingAdjustment adjustment = billingMapper.toFundingAdjustment(
                    billing,
                    idempotencyKey,
                    FundingAdjustmentStatus.NO_APLICA.name(),
                    now);
            adjustment.setCompletedAt(now);
            return fundingAdjustmentRepository.save(adjustment);
        }

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            FundingAdjustment adjustment = billingMapper.toFundingAdjustment(
                    billing,
                    idempotencyKey,
                    FundingAdjustmentStatus.FALLIDO.name(),
                    now);
            adjustment.setCoreResponseStatus("INCONSISTENT_REMAINING_AMOUNT");
            adjustment.setCoreResponseMessage("remainingAmount negativo; no se solicita liberacion de sobrante.");
            adjustment.setCompletedAt(now);
            return fundingAdjustmentRepository.save(adjustment);
        }

        if (billing.getCoreFundingId() == null || billing.getCoreFundingId().isBlank()) {
            FundingAdjustment adjustment = billingMapper.toFundingAdjustment(
                    billing,
                    idempotencyKey,
                    FundingAdjustmentStatus.FALLIDO.name(),
                    now);
            adjustment.setCoreResponseStatus("MISSING_CORE_FUNDING_ID");
            adjustment.setCoreResponseMessage("BatchLinesCompletedEvent no incluyo coreFundingId; no se usa fallback para liberacion.");
            adjustment.setCompletedAt(now);
            return fundingAdjustmentRepository.save(adjustment);
        }

        FundingAdjustment adjustment = billingMapper.toFundingAdjustment(
                billing,
                idempotencyKey,
                FundingAdjustmentStatus.SOLICITADO.name(),
                now);
        adjustment.setRequestedAt(now);
        fundingAdjustmentRepository.save(adjustment);

        try {
            CoreFundingReleaseResponse response = coreBankingClient.releaseFunding(toReleaseRequest(billing, idempotencyKey));
            applyFundingReleaseResponse(adjustment, response);
        } catch (CoreBankingClientException exception) {
            applyFundingReleaseFailure(adjustment, exception);
        }
        return adjustment;
    }

    private void applyFundingReleaseResponse(FundingAdjustment adjustment, CoreFundingReleaseResponse response) {
        OffsetDateTime completedAt = OffsetDateTime.now();
        Boolean released = RELEASED.equalsIgnoreCase(response.getStatus()) || APPROVED.equalsIgnoreCase(response.getStatus());
        adjustment.setStatus(released ? FundingAdjustmentStatus.LIBERADO.name() : FundingAdjustmentStatus.FALLIDO.name());
        adjustment.setCoreResponseStatus(response.getStatus());
        adjustment.setReleasedAmount(response.getReleasedAmount());
        adjustment.setCoreTransactionId(response.getCoreTransactionId());
        adjustment.setCoreResponseMessage(limitText(response.getMessage(), 500));
        adjustment.setCompletedAt(completedAt);
        adjustment.setUpdatedAt(completedAt);
        fundingAdjustmentRepository.save(adjustment);
    }

    private void applyFundingReleaseFailure(FundingAdjustment adjustment, CoreBankingClientException exception) {
        OffsetDateTime completedAt = OffsetDateTime.now();
        adjustment.setStatus(FundingAdjustmentStatus.FALLIDO.name());
        adjustment.setCoreResponseStatus(CoreRequestStatus.FALLIDO.name());
        adjustment.setCoreResponseMessage(limitText(exception.getMessage(), 500));
        adjustment.setCompletedAt(completedAt);
        adjustment.setUpdatedAt(completedAt);
        fundingAdjustmentRepository.save(adjustment);
        LOG.error("Fallo controlado al solicitar liberacion de sobrante al Core. batchId={}", adjustment.getBatchId(), exception);
    }

    private void applyCoreResponse(
            BatchBilling billing,
            BillingCoreRequest coreRequestRecord,
            CoreCommissionChargeResponse coreResponse) {
        OffsetDateTime respondedAt = OffsetDateTime.now();
        Boolean approved = APPROVED.equalsIgnoreCase(coreResponse.getStatus());
        billing.setStatus(approved ? BillingStatus.COBRO_COMISION_EXITOSO.name() : BillingStatus.COBRO_COMISION_FALLIDO.name());
        billing.setCoreResponseStatus(coreResponse.getStatus());
        billing.setCoreCommissionChargeId(coreResponse.getCoreCommissionChargeId());
        billing.setCoreTransactionId(coreResponse.getCoreTransactionId());
        billing.setTaxAmount(coreResponse.getTaxAmount());
        billing.setTotalChargedAmount(coreResponse.getTotalChargedAmount());
        billing.setCoreResponseMessage(limitText(coreResponse.getMessage(), 500));
        billing.setCoreRespondedAt(respondedAt);
        billing.setUpdatedAt(respondedAt);
        batchBillingRepository.save(billing);

        coreRequestRecord.setRequestStatus(approved ? CoreRequestStatus.APROBADO.name() : CoreRequestStatus.RECHAZADO.name());
        coreRequestRecord.setCoreResponseStatus(coreResponse.getStatus());
        coreRequestRecord.setCoreCommissionChargeId(coreResponse.getCoreCommissionChargeId());
        coreRequestRecord.setCoreTransactionId(coreResponse.getCoreTransactionId());
        coreRequestRecord.setTaxAmount(coreResponse.getTaxAmount());
        coreRequestRecord.setTotalChargedAmount(coreResponse.getTotalChargedAmount());
        coreRequestRecord.setCoreResponseMessage(limitText(coreResponse.getMessage(), 500));
        coreRequestRecord.setRespondedAt(respondedAt);
        billingCoreRequestRepository.save(coreRequestRecord);
    }

    private void applyCoreFailure(BatchBilling billing, BillingCoreRequest coreRequestRecord, CoreBankingClientException exception) {
        OffsetDateTime respondedAt = OffsetDateTime.now();
        String message = limitText(exception.getMessage(), 500);
        billing.setStatus(BillingStatus.COBRO_COMISION_FALLIDO.name());
        billing.setCoreResponseStatus(CoreRequestStatus.FALLIDO.name());
        billing.setCoreResponseMessage(message);
        billing.setCoreRespondedAt(respondedAt);
        billing.setUpdatedAt(respondedAt);
        batchBillingRepository.save(billing);

        coreRequestRecord.setRequestStatus(CoreRequestStatus.FALLIDO.name());
        coreRequestRecord.setCoreResponseStatus(CoreRequestStatus.FALLIDO.name());
        coreRequestRecord.setCoreResponseMessage(message);
        coreRequestRecord.setRespondedAt(respondedAt);
        billingCoreRequestRepository.save(coreRequestRecord);
        LOG.error("Fallo controlado al solicitar cobro de comision al Core. batchId={}", billing.getBatchId(), exception);
    }

    private void publishBillingCompleted(BatchBilling billing, FundingAdjustment fundingAdjustment) {
        if (Boolean.TRUE.equals(billing.getBillingCompletedEventPublished())) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        UUID eventId = UUID.randomUUID();
        BillingCompletedEvent event = billingMapper.toBillingCompletedEvent(billing, fundingAdjustment, eventId, now);
        billingCompletedEventPublisher.publish(event);
        billing.setBillingCompletedEventId(eventId);
        billing.setBillingCompletedEventPublished(true);
        billing.setCompletedAt(now);
        billing.setUpdatedAt(now);
        batchBillingRepository.save(billing);
    }

    private CoreFundingReleaseRequest toReleaseRequest(BatchBilling billing, String idempotencyKey) {
        CoreFundingReleaseRequest request = new CoreFundingReleaseRequest();
        request.setBatchId(billing.getBatchId());
        request.setCoreFundingId(billing.getCoreFundingId());
        request.setRemainingAmount(billing.getRemainingAmount());
        request.setCurrency(billing.getCurrency());
        request.setReason("Liberacion de sobrante operativo batch " + billing.getBatchId());
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }

    private CoreCommissionChargeRequest toCoreRequest(
            BatchBilling billing,
            String companyRuc,
            String sourceAccountNumber,
            String idempotencyKey) {
        CoreCommissionChargeRequest request = new CoreCommissionChargeRequest();
        request.setBatchId(billing.getBatchId());
        request.setCompanyRuc(companyRuc);
        request.setSourceAccountNumber(sourceAccountNumber);
        request.setBillableLines(billing.getBillableLines());
        request.setCommissionSubtotal(billing.getCommissionSubtotal());
        request.setCurrency(billing.getCurrency());
        request.setConcept("Comision pagos masivos batch " + billing.getBatchId());
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }

    private void validateEvent(BatchLinesCompletedEvent event) {
        if (event == null || event.getEventId() == null || event.getBatchId() == null || event.getCorrelationId() == null) {
            throw new IllegalArgumentException("BatchLinesCompletedEvent debe incluir eventId, batchId y correlationId");
        }
    }

    private String valueOrFallback(String value, String fallback, String fieldName, UUID batchId) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        LOG.warn("BatchLinesCompletedEvent sin {}. Se usa valor temporal configurado para compatibilidad. batchId={}",
                fieldName, batchId);
        return fallback;
    }

    private String limitText(String text, Integer maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
