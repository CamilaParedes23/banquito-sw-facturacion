package com.banquito.switchpagos.billing.repository;

import com.banquito.switchpagos.billing.model.CommissionCalculation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionCalculationRepository extends JpaRepository<CommissionCalculation, UUID> {

    Optional<CommissionCalculation> findByBatchId(UUID batchId);
}
