package com.erp.common.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

/**
 * Entry point for the "trusted in-process caller, no HTTP principal" pattern (see
 * {@code create-service/SKILL.md}'s "Cross-Module Calls (XM)" — "Internal trusted-caller calls").
 *
 * <p>Use this instead of leaving a target method entirely {@code @PreAuthorize}-ungated. The
 * target keeps a real, visible {@code @PreAuthorize("hasAuthority('} {@value #AUTHORITY}
 * {@code ')")} check — it just accepts this one synthetic authority instead of a real user
 * principal. Only code that explicitly calls {@link #call}/{@link #run} can ever produce that
 * authority; nothing in the JWT filter chain or any other authentication entry point grants it,
 * so it cannot be satisfied by an external HTTP request no matter how it's authenticated.
 *
 * <p>Restores whatever {@link SecurityContext} was present before the call (empty or a real
 * principal) once the call returns, mirroring this codebase's existing
 * {@code NotificationAsyncConfig.SecurityContextTaskDecorator} restore-on-finally discipline.
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
