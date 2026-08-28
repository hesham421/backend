package com.erp.common.domain.status;

import lombok.Getter;

/**
 * Generic result wrapper for service layer operations; carries a business {@link StatusCode}
 * rather than an HTTP status, keeping the service layer free of HTTP dependencies.
 */
@Getter
public class ServiceResult<T> {

    /**
     * The business status code
     */
    private final StatusCode status;

    /**
     * Message key or direct message
     */
    private final String message;

    /**
     * Result data (null on failure)
     */
    private final T data;

    /**
     * Additional details for errors
     */
    private final String details;

    private ServiceResult(StatusCode status, String message, T data, String details) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.details = details;
    }

    /**
     * Create a successful result with data
     */
    public static <T> ServiceResult<T> success(T data) {
        return new ServiceResult<>(Status.SUCCESS, null, data, null);
    }

    /**
     * Create a successful result with data and specific status
     */
    public static <T> ServiceResult<T> success(T data, StatusCode status) {
        return new ServiceResult<>(status, null, data, null);
    }

    /**
     * Create a successful result with data and message
     */
    public static <T> ServiceResult<T> success(T data, String message) {
        return new ServiceResult<>(Status.SUCCESS, message, data, null);
    }

    /**
     * Create a successful result with data, status, and message
     */
    public static <T> ServiceResult<T> success(T data, StatusCode status, String message) {
        return new ServiceResult<>(status, message, data, null);
    }

    /**
     * Create a success result without data (for void operations)
     */
    public static <T> ServiceResult<T> ok() {
        return new ServiceResult<>(Status.OK, null, null, null);
    }

    /**
     * Create a success result without data but with message
     */
    public static <T> ServiceResult<T> ok(String message) {
        return new ServiceResult<>(Status.OK, message, null, null);
    }

    /**
     * Create a failure result with status and message
     */
    public static <T> ServiceResult<T> failure(StatusCode status, String message) {
        return new ServiceResult<>(status, message, null, null);
    }

    /**
     * Create a failure result with status, message, and details
     */
    public static <T> ServiceResult<T> failure(StatusCode status, String message, String details) {
        return new ServiceResult<>(status, message, null, details);
    }

    /**
     * Create a validation error result
     */
    public static <T> ServiceResult<T> validationError(String message) {
        return failure(Status.VALIDATION_ERROR, message);
    }

    /**
     * Create a not found result
     */
    public static <T> ServiceResult<T> notFound(String message) {
        return failure(Status.NOT_FOUND, message);
    }

    /**
     * Create a business error result
     */
    public static <T> ServiceResult<T> businessError(String message) {
        return failure(Status.BUSINESS_RULE_VIOLATION, message);
    }

    /**
     * Check if this result indicates success
     */
    public boolean isSuccess() {
        return status != null && status.isSuccess();
    }

    /**
     * Check if this result indicates failure
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Check if this result has data
     */
    public boolean hasData() {
        return data != null;
    }

    /**
     * Get the status code string
     */
    public String getCode() {
        return status != null ? status.getCode() : null;
    }

    /**
     * Get the status category
     */
    public StatusCategory getCategory() {
        return status != null ? status.getCategory() : null;
    }
}
