package com.banquito.switchpagos.billing.exception;

public class CoreBankingClientException extends RuntimeException {

    public CoreBankingClientException(String message) {
        super(message);
    }

    public CoreBankingClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
