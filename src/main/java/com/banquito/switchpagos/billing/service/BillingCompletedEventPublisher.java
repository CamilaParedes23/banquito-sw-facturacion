package com.banquito.switchpagos.billing.service;

import com.banquito.switchpagos.billing.dto.event.BillingCompletedEvent;

public interface BillingCompletedEventPublisher {

    void publish(BillingCompletedEvent event);
}
