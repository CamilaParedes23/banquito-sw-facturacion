package com.banquito.switchpagos.billing.client;

import com.banquito.switchpagos.billing.dto.request.CoreCommissionChargeRequest;
import com.banquito.switchpagos.billing.dto.request.CoreFundingReleaseRequest;
import com.banquito.switchpagos.billing.dto.response.CoreCommissionChargeResponse;
import com.banquito.switchpagos.billing.dto.response.CoreFundingReleaseResponse;
import com.banquito.switchpagos.billing.exception.CoreBankingClientException;
import com.banquito.switchpagos.billing.grpc.core.CoreBankingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CoreBankingClient {

    private final CoreBankingServiceGrpc.CoreBankingServiceBlockingStub coreBankingStub;
    private final Integer deadlineMs;

    public CoreBankingClient(
            ManagedChannel coreBankingManagedChannel,
            @Value("${core.grpc.deadline-ms}") Integer deadlineMs) {
        this.coreBankingStub = CoreBankingServiceGrpc.newBlockingStub(coreBankingManagedChannel);
        this.deadlineMs = deadlineMs;
    }

    public CoreCommissionChargeResponse requestCommissionCharge(CoreCommissionChargeRequest request) {
        com.banquito.switchpagos.billing.grpc.core.CoreCommissionChargeRequest grpcRequest =
                com.banquito.switchpagos.billing.grpc.core.CoreCommissionChargeRequest.newBuilder()
                        .setBatchId(toString(request.getBatchId()))
                        .setCompanyRuc(toString(request.getCompanyRuc()))
                        .setSourceAccountNumber(toString(request.getSourceAccountNumber()))
                        .setBillableLines(request.getBillableLines() == null ? 0 : request.getBillableLines())
                        .setCommissionSubtotal(toString(request.getCommissionSubtotal()))
                        .setCurrency(toString(request.getCurrency()))
                        .setConcept(toString(request.getConcept()))
                        .setIdempotencyKey(toString(request.getIdempotencyKey()))
                        .build();
        try {
            return toResponse(coreBankingStub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
                    .requestCommissionCharge(grpcRequest));
        } catch (StatusRuntimeException exception) {
            throw new CoreBankingClientException("Error calling Core Banking gRPC service", exception);
        }
    }

    public CoreFundingReleaseResponse releaseFunding(CoreFundingReleaseRequest request) {
        com.banquito.switchpagos.billing.grpc.core.CoreFundingReleaseRequest grpcRequest =
                com.banquito.switchpagos.billing.grpc.core.CoreFundingReleaseRequest.newBuilder()
                        .setBatchId(toString(request.getBatchId()))
                        .setCoreFundingId(toString(request.getCoreFundingId()))
                        .setRemainingAmount(toString(request.getRemainingAmount()))
                        .setCurrency(toString(request.getCurrency()))
                        .setReason(toString(request.getReason()))
                        .setIdempotencyKey(toString(request.getIdempotencyKey()))
                        .build();
        try {
            return toReleaseResponse(coreBankingStub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
                    .releaseFunding(grpcRequest));
        } catch (StatusRuntimeException exception) {
            throw new CoreBankingClientException("Error calling Core Banking gRPC service for funding release", exception);
        }
    }

    private CoreCommissionChargeResponse toResponse(
            com.banquito.switchpagos.billing.grpc.core.CoreCommissionChargeResponse grpcResponse) {
        CoreCommissionChargeResponse response = new CoreCommissionChargeResponse();
        response.setBatchId(toUuid(grpcResponse.getBatchId()));
        response.setStatus(grpcResponse.getStatus());
        response.setCoreCommissionChargeId(grpcResponse.getCoreCommissionChargeId());
        response.setCommissionSubtotal(toBigDecimal(grpcResponse.getCommissionSubtotal()));
        response.setTaxAmount(toBigDecimal(grpcResponse.getTaxAmount()));
        response.setTotalChargedAmount(toBigDecimal(grpcResponse.getTotalChargedAmount()));
        response.setCoreTransactionId(grpcResponse.getCoreTransactionId());
        response.setMessage(grpcResponse.getMessage());
        return response;
    }

    private CoreFundingReleaseResponse toReleaseResponse(
            com.banquito.switchpagos.billing.grpc.core.CoreFundingReleaseResponse grpcResponse) {
        CoreFundingReleaseResponse response = new CoreFundingReleaseResponse();
        response.setBatchId(toUuid(grpcResponse.getBatchId()));
        response.setCoreFundingId(grpcResponse.getCoreFundingId());
        response.setStatus(grpcResponse.getStatus());
        response.setReleasedAmount(toBigDecimal(grpcResponse.getReleasedAmount()));
        response.setCoreTransactionId(grpcResponse.getCoreTransactionId());
        response.setMessage(grpcResponse.getMessage());
        return response;
    }

    private String toString(Object value) {
        return value == null ? "" : value.toString();
    }

    private UUID toUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private BigDecimal toBigDecimal(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }
}
