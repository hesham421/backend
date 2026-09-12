package com.erp.common.exception;

import com.erp.common.domain.status.Status;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class LocalizedException extends RuntimeException {

    private final Status status;
    private final String errorCode;
    private final transient Object[] args;

    /**
     * Every additional failure this exception reports beyond {@link #errorCode}'s own — empty for
     * every single-code throw, which is the overwhelming majority. See
     * {@link #LocalizedException(Status, List)}.
     */
    private final transient List<ErrorDetail> errors;

    /**
     * The single-code form — unchanged. {@link #errors} stays empty, so
     * {@code GlobalExceptionHandler} renders exactly the {@code code} / {@code message} envelope
     * it always has, with {@code ApiError.fieldErrors} left unset.
     */
    public LocalizedException(Status status, String errorCode, Object... args) {
        super(errorCode);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
        this.errors = List.of();
    }

    /**
     * The multi-error form (additive). Reports several business failures from one throw: the
     * first detail keeps the existing top-level {@code code}/{@code message} contract, and the
     * complete list — the first included — is surfaced through the {@code fieldErrors} list
     * {@link com.erp.common.web.ApiError} already exposes.
     *
     * <p>Introduced for REQ-FIN-015 / AC-FIN-015 ("returns both failures and posts nothing"). It
     * changes nothing for any existing caller: no existing constructor, {@code Status}, HTTP
     * mapping or {@code ApiError} field was altered.
     *
     * @param status the shared status for the aggregate — all collected failures must map to it
     * @param errors at least one failure; the first supplies the top-level code and arguments
     */
    public LocalizedException(Status status, List<ErrorDetail> errors) {
        super(Objects.requireNonNull(errors, "errors").get(0).errorCode());
        this.status = status;
        this.errorCode = errors.get(0).errorCode();
        this.args = errors.get(0).args();
        this.errors = List.copyOf(errors);
    }
}
