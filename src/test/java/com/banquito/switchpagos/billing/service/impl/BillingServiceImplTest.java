package com.banquito.switchpagos.billing.service.impl;

import com.banquito.switchpagos.billing.client.CoreBankingClient;
import com.banquito.switchpagos.billing.mapper.BillingMapper;
import com.banquito.switchpagos.billing.repository.BatchBillingRepository;
import com.banquito.switchpagos.billing.repository.BillingCoreRequestRepository;
import com.banquito.switchpagos.billing.repository.CommissionCalculationRepository;
import com.banquito.switchpagos.billing.service.BillingCompletedEventPublisher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class BillingServiceImplTest {

    private final BillingServiceImpl service = new BillingServiceImpl(
            mock(BatchBillingRepository.class),
            mock(CommissionCalculationRepository.class),
            mock(BillingCoreRequestRepository.class),
            mock(CoreBankingClient.class),
            mock(BillingCompletedEventPublisher.class),
            mock(BillingMapper.class),
            "1792103456001",
            "0010000010599");

    @Test
    void resolvesTariffByBillableLineVolume() {
        assertFee("0.00", 0);
        assertFee("0.50", 1);
        assertFee("0.50", 3);
        assertFee("0.50", 10);
        assertFee("0.40", 11);
        assertFee("0.40", 100);
        assertFee("0.30", 101);
        assertFee("0.30", 500);
        assertFee("0.20", 501);
        assertFee("0.20", 1000);
        assertFee("0.10", 1001);
        assertFee("0.10", 10000);
        assertFee("0.05", 10001);
    }

    private void assertFee(String expected, int billableLines) {
        assertEquals(new BigDecimal(expected), service.resolveUnitFee(billableLines));
    }
}
