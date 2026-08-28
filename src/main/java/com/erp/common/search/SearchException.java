package com.erp.common.search;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.BusinessException;

/**
 * Thrown when search/filter operations fail validation; always carries
 * {@link Status#VALIDATION_ERROR} (HTTP 400) rather than the {@link BusinessException} default of 422,
 * since these are always client-input errors.
 */
public class SearchException extends BusinessException {

    public SearchException(String code, String message) {
        super(Status.VALIDATION_ERROR, code, message);
    }

    /**
     * Uses SEARCH_ERROR as default error code for backward compatibility.
     *
     * @deprecated Use {@link #SearchException(String, String)} with explicit error code
     */
    @Deprecated
    public SearchException(String message) {
        super(Status.VALIDATION_ERROR, "SEARCH_ERROR", message);
    }

    /**
     * Uses SEARCH_ERROR as default error code for backward compatibility.
     *
     * @deprecated Use constructor with explicit error code
     */
    @Deprecated
    public SearchException(String message, Throwable cause) {
        super(Status.VALIDATION_ERROR, "SEARCH_ERROR", message, cause);
    }
}
