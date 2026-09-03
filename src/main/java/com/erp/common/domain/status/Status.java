package com.erp.common.domain.status;

import org.springframework.http.HttpStatus;

public enum Status {

    SUCCESS(HttpStatus.OK),
    CREATED(HttpStatus.CREATED),
    UPDATED(HttpStatus.OK),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    ALREADY_EXISTS(HttpStatus.CONFLICT),
    CONFLICT(HttpStatus.CONFLICT),
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_CONTENT),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST);

    private final HttpStatus httpStatus;

    Status(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
