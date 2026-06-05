package com.banquito.switchpagos.billing.repository;

import com.banquito.switchpagos.billing.model.BatchBilling;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchBillingRepository extends JpaRepository<BatchBilling, UUID> {

    Optional<BatchBilling> findByBatchId(UUID batchId);
}
