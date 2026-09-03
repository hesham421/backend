package com.erp.security.event;

import java.time.LocalDateTime;

/**
 * Published (fire-and-forget) when a user account is created and needs activation (API-SEC-007).
 * Carries the raw token so a downstream notifier can deliver it; only its hash is stored. SEC never
 * calls NOTIF directly (CORE.md / srs A7) — NOTIF, once built, listens for this ApplicationEvent.
 */
public record AccountActivationRequestedEvent(
    Long userAccountId,
    String email,
    String rawToken,
    LocalDateTime expiresAt) {
}
