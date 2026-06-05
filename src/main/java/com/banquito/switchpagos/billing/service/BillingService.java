package com.banquito.switchpagos.billing.service;

import com.banquito.switchpagos.billing.dto.event.BatchLinesCompletedEvent;

public interface BillingService {

    void processBatchLinesCompleted(BatchLinesCompletedEvent event);
}
