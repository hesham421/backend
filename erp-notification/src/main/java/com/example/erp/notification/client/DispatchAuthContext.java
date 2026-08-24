package com.example.erp.notification.client;

/**
 * Carries the triggering request's {@code Authorization} header value across the
 * notification-dispatch async boundary (see {@code NotificationAsyncConfig.RequestContextTaskDecorator}).
 *
 * <p>Deliberately holds just the header string, not the live {@code HttpServletRequest}/{@code
 * RequestAttributes} — the underlying request object is recycled by the servlet container once
 * the original response completes, which is typically already true by the time this dispatch
 * executor's worker thread picks up the task, so any later access to the live request throws
 * {@code IllegalStateException("The request object has been recycled...")}. A plain string has
 * no such lifecycle.
 */
public final class DispatchAuthContext {

    private static final ThreadLocal<String> AUTH_HEADER = new ThreadLocal<>();

    private DispatchAuthContext() {
    }

    public static void set(String authorizationHeader) {
        AUTH_HEADER.set(authorizationHeader);
    }

    public static String get() {
        return AUTH_HEADER.get();
    }

    public static void clear() {
        AUTH_HEADER.remove();
    }
}
