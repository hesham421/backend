package com.erp.common.domain.status;

/**
 * Domain-level grouping of business status codes; has no HTTP dependencies (HTTP mapping
 * is handled separately in OperationCodeImpl).
 */
public enum StatusCategory {

    /**
     * Operation completed successfully
     */
    SUCCESS,

    /**
     * Operation failed due to client error (bad input, validation failure)
     */
    CLIENT_ERROR,

    /**
     * Operation failed due to business rule violation
     */
    BUSINESS_ERROR,

    /**
     * Requested resource not found
     */
    NOT_FOUND,

    /**
     * Authentication or authorization failure
     */
    AUTH_ERROR,

    /**
     * Operation failed due to server/system error
     */
    SERVER_ERROR,

    /**
     * Conflict with current state (duplicate, concurrent modification)
     */
    CONFLICT
}
