package com.erp.common.exception;

import com.erp.common.domain.status.Status;
import com.erp.common.domain.status.StatusCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Localized exception carrying an i18n message key instead of a fixed message; message
 * resolution happens in {@code GlobalExceptionHandler} via {@code LocalizationService}.
 * Prefer the {@link StatusCode} constructors — the {@link HttpStatus} ones are deprecated.
 */
@Getter
public class LocalizedException extends RuntimeException {

    /**
     * Best-effort default, computed at construction time so {@link #getStatus()} always returns
     * something. NOT the authoritative response status: {@code GlobalExceptionHandler} resolves
     * the actual HTTP response status via {@code OperationCode.toHttpStatus(getStatusCode(), ...)},
     * using this only as the fallback when no mapping is found. Kept in sync with OperationCodeImpl
     * by convention, not by a shared code path (statically wiring exception -> web-layer OperationCode
     * would create a package cycle, since the web layer already depends on the exception layer).
     */
    private final HttpStatus status;
    private final StatusCode statusCode;
    private final String messageKey;
    private final Object[] args;
    
    public LocalizedException(StatusCode statusCode, String messageKey, Object... args) {
        super(messageKey);
        this.statusCode = statusCode;
        this.messageKey = messageKey;
        this.args = args;
        // Map to HttpStatus for backward compatibility
        this.status = mapStatusCodeToHttpStatus(statusCode);
    }
    
    /**
     * Defaults to {@link Status#BAD_REQUEST}.
     */
    public LocalizedException(String messageKey, Object... args) {
        this(Status.BAD_REQUEST, messageKey, args);
    }
    
    /**
     * @deprecated Use {@link #LocalizedException(StatusCode, String, Object...)} instead.
     * Service layer should not use HttpStatus directly.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public LocalizedException(HttpStatus status, String messageKey, Object... args) {
        super(messageKey);
        this.status = status;
        this.statusCode = mapHttpStatusToStatusCode(status);
        this.messageKey = messageKey;
        this.args = args;
    }
    
    /**
     * Maps StatusCode to HttpStatus for backward compatibility.
     * This logic should ideally be in OperationCode, but we inline it here
     * to avoid circular dependencies.
     */
    private static HttpStatus mapStatusCodeToHttpStatus(StatusCode statusCode) {
        if (statusCode == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return switch (statusCode.getCategory()) {
            case SUCCESS -> HttpStatus.OK;
            case CLIENT_ERROR -> HttpStatus.BAD_REQUEST;
            case BUSINESS_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case AUTH_ERROR -> {
                if (statusCode == Status.FORBIDDEN || statusCode == Status.ACCESS_DENIED) {
                    yield HttpStatus.FORBIDDEN;
                }
                yield HttpStatus.UNAUTHORIZED;
            }
            case SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
    }
    
    /**
     * Maps HttpStatus to StatusCode for backward compatibility with deprecated constructor.
     */
    private static StatusCode mapHttpStatusToStatusCode(HttpStatus httpStatus) {
        if (httpStatus == null) {
            return Status.BAD_REQUEST;
        }
        return switch (httpStatus) {
            case OK, CREATED -> Status.SUCCESS;
            case BAD_REQUEST -> Status.BAD_REQUEST;
            case NOT_FOUND -> Status.NOT_FOUND;
            case UNAUTHORIZED -> Status.UNAUTHORIZED;
            case FORBIDDEN -> Status.FORBIDDEN;
            case CONFLICT -> Status.DUPLICATE;
            case UNPROCESSABLE_ENTITY -> Status.BUSINESS_RULE_VIOLATION;
            default -> {
                if (httpStatus.is4xxClientError()) {
                    yield Status.BAD_REQUEST;
                } else if (httpStatus.is5xxServerError()) {
                    yield Status.INTERNAL_ERROR;
                }
                yield Status.BAD_REQUEST;
            }
        };
    }
}
