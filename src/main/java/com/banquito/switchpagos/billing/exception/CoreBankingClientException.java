package com.banquito.switchpagos.billing.exception;

public class CoreBankingClientException extends RuntimeException {

    private final Integer httpStatus;
    private final Boolean functionalRejection;

    public CoreBankingClientException(String message) {
        super(message);
        this.httpStatus = null;
        this.functionalRejection = Boolean.FALSE;
    }

    public CoreBankingClientException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.functionalRejection = Boolean.FALSE;
    }

    public CoreBankingClientException(String message, Integer httpStatus, Boolean functionalRejection, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.functionalRejection = functionalRejection;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public Boolean isFunctionalRejection() {
        return functionalRejection;
    }
}
