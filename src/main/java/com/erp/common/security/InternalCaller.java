package com.erp.common.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

/**
 * Grants {@link #AUTHORITY} for the duration of {@code action} so an in-process call can pass a
 * {@code @PreAuthorize("hasAuthority(...)")} check without a real principal; nothing in the JWT
 * filter chain can grant this authority, so it is unreachable from an external HTTP request.
 * Restores the prior {@link SecurityContext} afterward.
 */
public final class InternalCaller {

    public static final String AUTHORITY = "INTERNAL_TRUSTED_CALLER";

    private InternalCaller() {
    }

    /** Runs {@code action} with {@link #AUTHORITY} on the current thread, then restores the prior context. */
    public static <T> T call(Supplier<T> action) {
        SecurityContext previous = SecurityContextHolder.getContext();
        try {
            SecurityContext internalContext = SecurityContextHolder.createEmptyContext();
            internalContext.setAuthentication(new InternalCallerAuthentication());
            SecurityContextHolder.setContext(internalContext);
            return action.get();
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }

    /** {@link #call} for a {@code void} action. */
    public static void run(Runnable action) {
        call(() -> {
            action.run();
            return null;
        });
    }
}
