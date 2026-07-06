package com.example.erp.common.exception;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.domain.status.StatusCode;
import lombok.Getter;

/**
 * Exception for business logic violations.
 * HTTP status is resolved from {@link #statusCode} via OperationCode; defaults to
 * {@link Status#BUSINESS_RULE_VIOLATION} (422) for callers that don't specify one.
 *
 * @author ERP Team
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * Machine-readable error code (e.g., DUPLICATE_ACCOUNT_CODE, PERIOD_CLOSED)
     */
    private final String code;

    /**
     * Additional details about the error
     */
    private final String details;

    /**
     * Domain-level status driving the HTTP status mapping (web layer resolves this via OperationCode).
     */
    private final StatusCode statusCode;

    public BusinessException(String code, String message) {
        this(Status.BUSINESS_RULE_VIOLATION, code, message, null, null);
    }

    public BusinessException(String code, String message, String details) {
        this(Status.BUSINESS_RULE_VIOLATION, code, message, details, null);
    }

    public BusinessException(String code, String message, Throwable cause) {
        this(Status.BUSINESS_RULE_VIOLATION, code, message, null, cause);
    }

    public BusinessException(String code, String message, String details, Throwable cause) {
        this(Status.BUSINESS_RULE_VIOLATION, code, message, details, cause);
    }

    public BusinessException(StatusCode statusCode, String code, String message) {
        this(statusCode, code, message, null, null);
    }

    public BusinessException(StatusCode statusCode, String code, String message, String details) {
        this(statusCode, code, message, details, null);
    }

    public BusinessException(StatusCode statusCode, String code, String message, Throwable cause) {
        this(statusCode, code, message, null, cause);
    }

    private BusinessException(StatusCode statusCode, String code, String message, String details, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.code = code;
        this.details = details;
    }
}
