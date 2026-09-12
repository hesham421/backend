package com.erp.common.exception;

/**
 * One business failure, carrying exactly what a single-code {@link LocalizedException} carries:
 * a registered error code plus the arguments its message bundle entry substitutes.
 *
 * <p>Shared infrastructure, added additively so that one {@code LocalizedException} can report
 * several failures at once (REQ-FIN-015 / AC-FIN-015 — "returns both failures and posts
 * nothing"). It is deliberately minimal: no field name, no severity, no builder. Each detail
 * localizes through {@code GlobalExceptionHandler}'s existing {@code MessageSource} lookup
 * exactly as a single-code exception does.
 *
 * <p>A domain class produces one of these from a non-throwing guard sibling (for example
 * {@code checkBalanced(...)}), and its throwing counterpart turns it straight back into a
 * {@code LocalizedException} — so every rule keeps ONE implementation.
 *
 * @param errorCode a code registered in a module's {@code <Module>ErrorCodes} class
 * @param args      the message-bundle substitution arguments, may be empty
 */
public record ErrorDetail(String errorCode, Object... args) {

    /** Convenience factory reading exactly like a {@code LocalizedException} construction. */
    public static ErrorDetail of(String errorCode, Object... args) {
        return new ErrorDetail(errorCode, args);
    }
}
