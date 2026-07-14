package com.example.erp.notification.exception;

/**
 * Centralized Error Codes for Notification Service Module.
 *
 * All codes must have corresponding messages in:
 * - erp-main/src/main/resources/i18n/messages.properties (English)
 * - erp-main/src/main/resources/i18n/messages_ar.properties (Arabic)
 */
public final class NotificationErrorCodes {

    private NotificationErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== Event Processing Errors (SVCAPI — INTERNAL EVENT PROCESSING) ====================
    // ERR-NOTIF-0001 (RULE-NOTIF-001 — event contract completeness, API-NOTIF-001/002)
    public static final String NOTIF_EVENT_INCOMPLETE = "NOTIF_EVENT_INCOMPLETE";

    // ==================== History / Unread / Read Errors (SVCAPI Layer 2 — API-NOTIF-003/004/005) ====================
    // API-NOTIF-003 — current user's numeric id could not be resolved via SecurityUserClient
    public static final String NOTIF_CURRENT_USER_UNRESOLVED = "NOTIF_CURRENT_USER_UNRESOLVED";
    // API-NOTIF-004/005 — GOVERNANCE-NOTE-BLOCKED pending DRV-NOTIF-003 (no read/unread column)
    public static final String NOTIF_READ_TRACKING_UNAVAILABLE = "NOTIF_READ_TRACKING_UNAVAILABLE";

    // ==================== Template Errors (SVCAPI Layer 3 — API-NOTIF-006..010) ====================
    public static final String NOTIF_TEMPLATE_NOT_FOUND = "NOTIF_TEMPLATE_NOT_FOUND";
    // ERR-NOTIF-0002 (RULE-NOTIF-006 — bilingual requirement, create/update) -> HTTP 400
    public static final String NOTIF_TEMPLATE_BILINGUAL_REQUIRED = "NOTIF_TEMPLATE_BILINGUAL_REQUIRED";
    // ERR-NOTIF-0003 (RULE-NOTIF-007 — templateCode uniqueness) -> HTTP 409
    public static final String NOTIF_TEMPLATE_CODE_DUPLICATE = "NOTIF_TEMPLATE_CODE_DUPLICATE";

    // ==================== Channel Config Errors (SVCAPI Layer 4 — API-NOTIF-011/012) ====================
    // "platform-standard 404" per SVCAPI.md — no dedicated RULE-ID/ERR-ID assigned
    public static final String NOTIF_CHANNEL_CONFIG_NOT_FOUND = "NOTIF_CHANNEL_CONFIG_NOT_FOUND";
}
