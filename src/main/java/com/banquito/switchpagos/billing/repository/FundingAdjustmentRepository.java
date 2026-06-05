package com.banquito.switchpagos.billing.repository;

import com.banquito.switchpagos.billing.model.FundingAdjustment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingAdjustmentRepository extends JpaRepository<FundingAdjustment, UUID> {

    Optional<FundingAdjustment> findByBatchId(UUID batchId);

    Optional<FundingAdjustment> findByIdempotencyKey(String idempotencyKey);
}
