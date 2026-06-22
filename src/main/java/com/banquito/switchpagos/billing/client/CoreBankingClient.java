package com.banquito.switchpagos.billing.client;

import com.banquito.switchpagos.billing.dto.request.CoreCommissionChargeRequest;
import com.banquito.switchpagos.billing.dto.request.CoreFundingReleaseRequest;
import com.banquito.switchpagos.billing.dto.request.CoreServiceFeeChargeRequest;
import com.banquito.switchpagos.billing.dto.response.CoreCommissionChargeResponse;
import com.banquito.switchpagos.billing.dto.response.CoreReservationResponse;
import com.banquito.switchpagos.billing.exception.CoreBankingClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
public class CoreBankingClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreBankingClient.class);

    private final RestClient coreKongRestClient;
    private final String switchCorePath;
    private final String paymentReservationsPath;
    private final CoreKongTokenProvider tokenProvider;

    public CoreBankingClient(
            @Qualifier("coreKongRestClient") RestClient coreKongRestClient,
            @Value("${core.kong.switch-core-path}") String switchCorePath,
            @Value("${core.kong.payment-reservations-path}") String paymentReservationsPath,
            CoreKongTokenProvider tokenProvider) {
        this.coreKongRestClient = coreKongRestClient;
        this.switchCorePath = switchCorePath;
        this.paymentReservationsPath = paymentReservationsPath;
        this.tokenProvider = tokenProvider;
        LOGGER.info(
                "Billing Core R9I fee contract activo: payload=commissionSubtotal-only, responseFields=chargedCommission*, feeTransactionUuid, feeJournalEntryUuid");
    }

    public CoreCommissionChargeResponse requestCommissionCharge(CoreCommissionChargeRequest request) {
        validateCommissionRequest(request);
        CoreServiceFeeChargeRequest coreRequest = toCoreRequest(request);
        String uri = buildServiceFeeChargeUri(request.getCoreFundingId());
        try {
            CoreReservationResponse coreResponse = coreKongRestClient.post()
                    .uri(uri)
                    .headers(headers -> applyAuthorization(headers))
                    .body(coreRequest)
                    .retrieve()
                    .body(CoreReservationResponse.class);
            return toCommissionResponse(request, coreResponse);
        } catch (RestClientResponseException exception) {
            Integer statusCode = exception.getStatusCode().value();
            Boolean functionalRejection = isFunctionalRejection(statusCode);
            String message = "Core REST/Kong rechazo cobro de comision. httpStatus=" + statusCode
                    + ", body=" + sanitizeBody(exception.getResponseBodyAsString());
            throw new CoreBankingClientException(message, statusCode, functionalRejection, exception);
        } catch (ResourceAccessException exception) {
            throw new CoreBankingClientException("Error tecnico de conectividad contra Core REST/Kong", null, Boolean.FALSE, exception);
        } catch (RuntimeException exception) {
            throw new CoreBankingClientException("Error tecnico al invocar Core REST/Kong", null, Boolean.FALSE, exception);
        }
    }

    public void releaseFunding(CoreFundingReleaseRequest request) {
        throw new CoreBankingClientException("ReleaseFunding es legacy/deprecado y no se usa en la regla vigente de sobrante");
    }

    private CoreServiceFeeChargeRequest toCoreRequest(CoreCommissionChargeRequest request) {
        CoreServiceFeeChargeRequest coreRequest = new CoreServiceFeeChargeRequest();
        coreRequest.setCommissionSubtotal(request.getCommissionSubtotal());
        coreRequest.setCorrelationId(request.getCorrelationId());
        coreRequest.setExternalReference(request.getIdempotencyKey());
        return coreRequest;
    }

    private CoreCommissionChargeResponse toCommissionResponse(
            CoreCommissionChargeRequest request,
            CoreReservationResponse coreResponse) {
        CoreCommissionChargeResponse response = new CoreCommissionChargeResponse();
        response.setBatchId(request.getBatchId());
        response.setStatus(coreResponse == null ? null : coreResponse.status());
        response.setCoreCommissionChargeId(coreResponse == null ? null : coreResponse.feeTransactionUuid());
        response.setCommissionSubtotal(resolveCommissionSubtotal(request, coreResponse));
        response.setTaxAmount(coreResponse == null ? null
                : firstNonNull(coreResponse.chargedCommissionTaxAmount(), coreResponse.taxAmount()));
        response.setTotalChargedAmount(coreResponse == null ? null
                : firstNonNull(coreResponse.chargedCommissionTotalAmount(), coreResponse.totalChargedAmount()));
        response.setCoreTransactionId(coreResponse == null ? null : coreResponse.feeJournalEntryUuid());
        response.setMessage(coreResponse == null ? "Core response null" : "Cobro de comisión procesado por Core");
        LOGGER.info(
                "Core service-fee-charge procesado. batchId={}, status={}, commissionSubtotal={}, taxAmount={}, totalChargedAmount={}, feeTransactionUuidPresent={}, feeJournalEntryUuidPresent={}",
                request.getBatchId(),
                response.getStatus(),
                response.getCommissionSubtotal(),
                response.getTaxAmount(),
                response.getTotalChargedAmount(),
                response.getCoreCommissionChargeId() != null,
                response.getCoreTransactionId() != null);
        return response;
    }

    private BigDecimal resolveCommissionSubtotal(
            CoreCommissionChargeRequest request,
            CoreReservationResponse coreResponse) {
        if (coreResponse == null) {
            return request.getCommissionSubtotal();
        }
        return firstNonNull(
                firstNonNull(coreResponse.commissionSubtotal(), coreResponse.chargedCommissionSubtotal()),
                request.getCommissionSubtotal());
    }

    private BigDecimal firstNonNull(BigDecimal preferred, BigDecimal fallback) {
        return preferred != null ? preferred : fallback;
    }

    private void validateCommissionRequest(CoreCommissionChargeRequest request) {
        if (request == null) {
            throw new CoreBankingClientException("CoreCommissionChargeRequest no puede ser null");
        }
        if (request.getCoreFundingId() == null || request.getCoreFundingId().isBlank()) {
            throw new CoreBankingClientException("coreFundingId legacy es requerido y debe contener reservationUuid");
        }
        if (request.getBatchId() == null || request.getCorrelationId() == null) {
            throw new CoreBankingClientException("batchId y correlationId son requeridos para cobro de comision");
        }
        if (request.getCommissionSubtotal() == null) {
            throw new CoreBankingClientException("commissionSubtotal es requerido para cobro de comision");
        }
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new CoreBankingClientException("externalReference/idempotencyKey es requerido para cobro de comision");
        }
    }

    private String buildServiceFeeChargeUri(String reservationUuid) {
        return normalizePath(switchCorePath)
                + normalizePath(paymentReservationsPath)
                + "/"
                + reservationUuid
                + "/service-fee-charge";
    }

    private String normalizePath(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private void applyAuthorization(HttpHeaders headers) {
        headers.setBearerAuth(tokenProvider.getBearerToken());
    }

    private Boolean isFunctionalRejection(Integer httpStatus) {
        return httpStatus != null && (httpStatus == 400 || httpStatus == 404 || httpStatus == 409 || httpStatus == 422);
    }

    private String sanitizeBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        String normalized = responseBody.replace('\n', ' ').replace('\r', ' ').trim();
        if (normalized.length() <= 300) {
            return normalized;
        }
        return normalized.substring(0, 300);
    }
}
