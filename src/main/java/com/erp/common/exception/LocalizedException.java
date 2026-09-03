package com.erp.common.exception;

import com.erp.common.domain.status.Status;
import lombok.Getter;

@Getter
public class LocalizedException extends RuntimeException {

    private final Status status;
    private final String errorCode;
    private final transient Object[] args;

    public LocalizedException(Status status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
    }
}
