package com.banquito.switchpagos.billing.service.impl;

import com.banquito.switchpagos.billing.client.CoreBankingClient;
import com.banquito.switchpagos.billing.dto.event.BatchLinesCompletedEvent;
import com.banquito.switchpagos.billing.dto.event.BillingCompletedEvent;
import com.banquito.switchpagos.billing.dto.request.CoreCommissionChargeRequest;
import com.banquito.switchpagos.billing.dto.response.CoreCommissionChargeResponse;
import com.banquito.switchpagos.billing.enums.BillingStatus;
import com.banquito.switchpagos.billing.enums.CoreRequestStatus;
import com.banquito.switchpagos.billing.exception.CoreBankingClientException;
import com.banquito.switchpagos.billing.mapper.BillingMapper;
import com.banquito.switchpagos.billing.model.BatchBilling;
import com.banquito.switchpagos.billing.model.BillingCoreRequest;
import com.banquito.switchpagos.billing.repository.BatchBillingRepository;
import com.banquito.switchpagos.billing.repository.BillingCoreRequestRepository;
import com.banquito.switchpagos.billing.repository.CommissionCalculationRepository;
import com.banquito.switchpagos.billing.service.BillingCompletedEventPublisher;
import com.banquito.switchpagos.billing.service.BillingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingServiceImpl implements BillingService {

    private static final Logger LOG = LoggerFactory.getLogger(BillingServiceImpl.class);
    private static final Set<String> CORE_SUCCESS_STATUSES = Set.of(
            "APPROVED",
            "ACTIVA",
            "CONSUMIDA",
            "CONSUMIDA_PARCIAL",
            "CONSUMIDA_TOTAL");

    private final BatchBillingRepository batchBillingRepository;
    private final CommissionCalculationRepository commissionCalculationRepository;
    private final BillingCoreRequestRepository billingCoreRequestRepository;
    private final CoreBankingClient coreBankingClient;
    private final BillingCompletedEventPublisher billingCompletedEventPublisher;
    private final BillingMapper billingMapper;
    private final String fallbackCompanyRuc;
    private final String fallbackSourceAccountNumber;

    public BillingServiceImpl(
            BatchBillingRepository batchBillingRepository,
            CommissionCalculationRepository commissionCalculationRepository,
            BillingCoreRequestRepository billingCoreRequestRepository,
            CoreBankingClient coreBankingClient,
            BillingCompletedEventPublisher billingCompletedEventPublisher,
            BillingMapper billingMapper,
            @Value("${switch.billing.fallback-company-ruc}") String fallbackCompanyRuc,
            @Value("${switch.billing.fallback-source-account-number}") String fallbackSourceAccountNumber) {
        this.batchBillingRepository = batchBillingRepository;
        this.commissionCalculationRepository = commissionCalculationRepository;
        this.billingCoreRequestRepository = billingCoreRequestRepository;
        this.coreBankingClient = coreBankingClient;
        this.billingCompletedEventPublisher = billingCompletedEventPublisher;
        this.billingMapper = billingMapper;
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
        BigDecimal unitFee = resolveUnitFee(billableLines);
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
        logRemainingAmountAsInformativeMetric(billing);

        if (billing.getCoreFundingId() == null || billing.getCoreFundingId().isBlank()) {
            billing.setStatus(BillingStatus.COBRO_COMISION_FALLIDO.name());
            billing.setCoreResponseStatus("MISSING_RESERVATION_UUID");
            billing.setCoreResponseMessage("BatchLinesCompletedEvent no incluyo coreFundingId/reservationUuid; no se puede cobrar comision via Core REST.");
            billing.setUpdatedAt(OffsetDateTime.now());
            batchBillingRepository.save(billing);
            publishBillingCompleted(billing);
            LOG.warn("Cobro de comision omitido por falta de reservationUuid/coreFundingId. batchId={}", event.getBatchId());
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

        publishBillingCompleted(billing);
        LOG.info("Billing procesado. batchId={}, billableLines={}, commissionSubtotal={}, status={}",
                event.getBatchId(), billableLines, commissionSubtotal, billing.getStatus());
    }

    BigDecimal resolveUnitFee(Integer billableLines) {
        int volume = billableLines == null ? 0 : billableLines;
        if (volume <= 0) {
            return amount("0.00");
        }
        if (volume <= 10) {
            return amount("0.50");
        }
        if (volume <= 100) {
            return amount("0.40");
        }
        if (volume <= 500) {
            return amount("0.30");
        }
        if (volume <= 1000) {
            return amount("0.20");
        }
        if (volume <= 10000) {
            return amount("0.10");
        }
        return amount("0.05");
    }

    private BigDecimal amount(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private void logRemainingAmountAsInformativeMetric(BatchBilling billing) {
        BigDecimal remainingAmount = billing.getRemainingAmount() == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : billing.getRemainingAmount().setScale(2, RoundingMode.HALF_UP);
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            LOG.info("remainingAmount informativo no devuelto por regla vigente. batchId={}, remainingAmount={}",
                    billing.getBatchId(), remainingAmount);
        } else if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            LOG.warn("remainingAmount negativo registrado como inconsistencia informativa; no bloquea comision. batchId={}, remainingAmount={}",
                    billing.getBatchId(), remainingAmount);
        }
    }

    private void applyCoreResponse(
            BatchBilling billing,
            BillingCoreRequest coreRequestRecord,
            CoreCommissionChargeResponse coreResponse) {
        OffsetDateTime respondedAt = OffsetDateTime.now();
        Boolean approved = isApproved(coreResponse.getStatus());
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

    private Boolean isApproved(String coreStatus) {
        if (coreStatus == null || coreStatus.isBlank()) {
            return Boolean.FALSE;
        }
        return CORE_SUCCESS_STATUSES.contains(coreStatus.trim().toUpperCase());
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
        coreRequestRecord.setCoreResponseStatus(exception.getHttpStatus() == null
                ? CoreRequestStatus.FALLIDO.name()
                : "HTTP_" + exception.getHttpStatus());
        coreRequestRecord.setCoreResponseMessage(message);
        coreRequestRecord.setRespondedAt(respondedAt);
        billingCoreRequestRepository.save(coreRequestRecord);
        LOG.error("Fallo controlado al solicitar cobro de comision al Core. batchId={}", billing.getBatchId(), exception);
    }

    private void publishBillingCompleted(BatchBilling billing) {
        if (Boolean.TRUE.equals(billing.getBillingCompletedEventPublished())) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        UUID eventId = UUID.randomUUID();
        BillingCompletedEvent event = billingMapper.toBillingCompletedEvent(billing, null, eventId, now);
        billingCompletedEventPublisher.publish(event);
        billing.setBillingCompletedEventId(eventId);
        billing.setBillingCompletedEventPublished(true);
        billing.setCompletedAt(now);
        billing.setUpdatedAt(now);
        batchBillingRepository.save(billing);
    }

    private CoreCommissionChargeRequest toCoreRequest(
            BatchBilling billing,
            String companyRuc,
            String sourceAccountNumber,
            String idempotencyKey) {
        CoreCommissionChargeRequest request = new CoreCommissionChargeRequest();
        request.setBatchId(billing.getBatchId());
        request.setCorrelationId(billing.getCorrelationId());
        request.setCoreFundingId(billing.getCoreFundingId());
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
