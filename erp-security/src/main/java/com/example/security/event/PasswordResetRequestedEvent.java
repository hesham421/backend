package com.example.security.event;

import java.time.Instant;

/**
 * RULE-SEC-031 — published instead of calling NotificationService directly (Conflict #20 /
 * BLK-SEC-002 resolution, execution-plan-SEC-gaps.md Section 6.2). See
 * {@link AccountActivationRequestedEvent} for the full rationale (same pattern, same
 * ApplicationEventPublisher mechanism).
 */
public record PasswordResetRequestedEvent(Long userIdFk, String token, Instant expiresAt) {
}
