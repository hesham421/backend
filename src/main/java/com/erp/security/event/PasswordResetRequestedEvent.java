package com.erp.security.event;

import java.util.Map;

/**
 * RULE-SEC-031, same pattern as {@link AccountActivationRequestedEvent}. Carries the
 * fully-assembled contextData rather than the raw token/expiry, so the listener never
 * re-derives or logs them.
 */
public record PasswordResetRequestedEvent(Long userIdFk, Map<String, Object> contextData) {
}
