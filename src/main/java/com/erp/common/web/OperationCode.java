package com.erp.common.web;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Maps business {@link StatusCode}s to HTTP status codes; belongs to the web layer only —
 * the service layer must not depend on it.
 */
public interface OperationCode {

    HttpStatus toHttpStatus(StatusCode statusCode);

    /**
     * @param defaultStatus used when no mapping is found for {@code statusCode}
     */
    HttpStatus toHttpStatus(StatusCode statusCode, HttpStatus defaultStatus);

    boolean isSuccessHttpStatus(StatusCode statusCode);

    /**
     * The only sanctioned translation point from service layer to HTTP; controllers must use this
     * rather than manually building responses.
     */
    <T> ResponseEntity<ApiResponse<T>> craftResponse(ServiceResult<T> result);
}
