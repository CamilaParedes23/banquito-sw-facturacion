package com.banquito.switchpagos.billing.repository;

import com.banquito.switchpagos.billing.model.BillingCoreRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingCoreRequestRepository extends JpaRepository<BillingCoreRequest, UUID> {

    Optional<BillingCoreRequest> findByBatchId(UUID batchId);

    Optional<BillingCoreRequest> findByIdempotencyKey(String idempotencyKey);
}
