package com.example.security.event;

import java.util.Map;

/**
 * RULE-SEC-031 — published instead of calling NotificationService directly (Conflict #20 /
 * BLK-SEC-002 resolution, execution-plan-SEC-gaps.md Section 6.2). See
 * {@link AccountActivationRequestedEvent} for the full rationale (same pattern, same
 * ApplicationEventPublisher mechanism).
 *
 * <p>Carries the fully-assembled notification contextData (see
 * {@code PasswordResetEmailContextBuilder}) rather than the raw token/expiry, so the listener
 * never re-derives or logs them.
 */
public record PasswordResetRequestedEvent(Long userIdFk, Map<String, Object> contextData) {
}
