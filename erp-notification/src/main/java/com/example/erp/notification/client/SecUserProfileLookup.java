package com.example.erp.notification.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal local view of erp-security's {@code SecUserProfileDto} (GET
 * /api/v1/security/user-profiles/{userId}). erp-notification has no Maven dependency on
 * erp-security (recipientId is a plain scalar FK per NotificationLog's javadoc, same pattern),
 * so only {@code preferredLang} — needed for RULE-NOTIF-006's language resolution — is
 * declared here; all other JSON properties on the real response are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SecUserProfileLookup(Long userIdFk, String preferredLang) {
}
