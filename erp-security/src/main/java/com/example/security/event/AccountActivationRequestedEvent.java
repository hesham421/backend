package com.example.security.event;

import java.time.Instant;

/**
 * RULE-SEC-031 — published instead of calling NotificationService directly (Conflict #20 /
 * BLK-SEC-002 resolution, execution-plan-SEC-gaps.md Section 6.2). NotificationService
 * (not yet implemented) is expected to subscribe and send the activation email; Security
 * has no compile-time dependency on it. Published via Spring's ApplicationEventPublisher —
 * no other event-publishing mechanism exists yet anywhere in this codebase (confirmed during
 * Phase 3 research), so this is the first one, per the plan's explicit fallback instruction.
 */
public record AccountActivationRequestedEvent(Long userIdFk, String token, Instant expiresAt) {
}
