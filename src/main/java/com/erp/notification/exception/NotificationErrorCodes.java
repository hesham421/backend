package com.erp.notification.exception;

/**
 * Centralized error codes for the Notification module; every code needs matching entries in the English and Arabic i18n message files.
 */
public final class NotificationErrorCodes {

    private NotificationErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ERR-NOTIF-0001 (RULE-NOTIF-001 — event contract completeness, API-NOTIF-001/002)
    public static final String NOTIF_EVENT_INCOMPLETE = "NOTIF_EVENT_INCOMPLETE";

    // API-NOTIF-003 — current user's numeric id could not be resolved via SecurityUserClient
    public static final String NOTIF_CURRENT_USER_UNRESOLVED = "NOTIF_CURRENT_USER_UNRESOLVED";
    // API-NOTIF-004/005 — GOVERNANCE-NOTE-BLOCKED pending DRV-NOTIF-003 (no read/unread column)
    public static final String NOTIF_READ_TRACKING_UNAVAILABLE = "NOTIF_READ_TRACKING_UNAVAILABLE";

    public static final String NOTIF_TEMPLATE_NOT_FOUND = "NOTIF_TEMPLATE_NOT_FOUND";
    // ERR-NOTIF-0002 (RULE-NOTIF-006 — bilingual requirement, create/update) -> HTTP 400
    public static final String NOTIF_TEMPLATE_BILINGUAL_REQUIRED = "NOTIF_TEMPLATE_BILINGUAL_REQUIRED";
    // ERR-NOTIF-0003 (RULE-NOTIF-007 — templateCode uniqueness) -> HTTP 409
    public static final String NOTIF_TEMPLATE_CODE_DUPLICATE = "NOTIF_TEMPLATE_CODE_DUPLICATE";

    // "platform-standard 404" per SVCAPI.md — no dedicated RULE-ID/ERR-ID assigned
    public static final String NOTIF_CHANNEL_CONFIG_NOT_FOUND = "NOTIF_CHANNEL_CONFIG_NOT_FOUND";
}
