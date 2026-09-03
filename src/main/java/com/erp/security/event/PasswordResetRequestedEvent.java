package com.erp.security.event;

import java.time.LocalDateTime;

/**
 * Published (fire-and-forget) when a password reset is requested (API-SEC-004). Carries the raw
 * token so a downstream notifier can deliver it; only its hash is stored. SEC never calls NOTIF
 * directly (CORE.md / srs A7) — NOTIF, once built, listens for this in-process ApplicationEvent.
 */
public record PasswordResetRequestedEvent(
    Long userAccountId,
    String email,
    String rawToken,
    LocalDateTime expiresAt) {
}
