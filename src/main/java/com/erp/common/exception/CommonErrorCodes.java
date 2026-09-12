package com.erp.common.exception;

public final class CommonErrorCodes {

    private CommonErrorCodes() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String DATA_INTEGRITY_VIOLATION = "DATA_INTEGRITY_VIOLATION";
}
