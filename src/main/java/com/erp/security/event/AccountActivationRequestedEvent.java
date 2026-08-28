package com.erp.security.event;

import java.time.Instant;

/**
 * RULE-SEC-031: published instead of calling NotificationService directly, so Security keeps
 * no compile-time dependency on it; NotificationService is expected to subscribe and send
 * the activation email.
 */
public record AccountActivationRequestedEvent(Long userIdFk, String token, Instant expiresAt) {
}
