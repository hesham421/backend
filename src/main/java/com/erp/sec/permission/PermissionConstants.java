package com.erp.sec.permission;

/**
 * Platform permission codes, shaped {@code PERM_<PAGE_CODE>_<ACTION>}
 * (profile.conventions.security_model.permission_pattern, srs-sec.md §1 DBF-SEC-050). Only the
 * codes an implemented API's {@code Security :} line names are declared; the permission matrix and
 * its seed data belong to the SEC-BE phase.
 */
public final class PermissionConstants {

    private PermissionConstants() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /** API-SEC-006 — screen SEC_USERS. */
    public static final String PERM_SEC_USERS_CREATE = "PERM_SEC_USERS_CREATE";

    /** API-SEC-007, 008, 009, 010, 011 — screen SEC_USERS. */
    public static final String PERM_SEC_USERS_UPDATE = "PERM_SEC_USERS_UPDATE";

    /** API-SEC-013 — screen SEC_ROLES. */
    public static final String PERM_SEC_ROLES_CREATE = "PERM_SEC_ROLES_CREATE";

    /** API-SEC-014, 015, 016, 017 — screen SEC_ROLES. */
    public static final String PERM_SEC_ROLES_UPDATE = "PERM_SEC_ROLES_UPDATE";

    /** API-SEC-026 — screen SEC_SESSIONS. */
    public static final String PERM_SEC_SESSIONS_DELETE = "PERM_SEC_SESSIONS_DELETE";

    /** API-SEC-018, 019, 020 — screen SEC_MODULE_REGISTRY. */
    public static final String PERM_SEC_MODULE_REGISTRY_UPDATE = "PERM_SEC_MODULE_REGISTRY_UPDATE";

    /** API-SEC-024 — screen SEC_AUDIT_LOG; search and export share this VIEW permission. */
    public static final String PERM_SEC_AUDIT_LOG_VIEW = "PERM_SEC_AUDIT_LOG_VIEW";

    /** API-SEC-005 — screen SEC_USERS. */
    public static final String PERM_SEC_USERS_VIEW = "PERM_SEC_USERS_VIEW";

    /** API-SEC-012 — screen SEC_ROLES. */
    public static final String PERM_SEC_ROLES_VIEW = "PERM_SEC_ROLES_VIEW";

    /** API-SEC-021 — screen SEC_MODULE_REGISTRY. */
    public static final String PERM_SEC_MODULE_REGISTRY_VIEW = "PERM_SEC_MODULE_REGISTRY_VIEW";

    /** API-SEC-022 — screen SEC_DASHBOARD; the gateway permission, per-widget VIEWs apply on top. */
    public static final String PERM_SEC_DASHBOARD_VIEW = "PERM_SEC_DASHBOARD_VIEW";

    /** API-SEC-025 — screen SEC_SESSIONS. */
    public static final String PERM_SEC_SESSIONS_VIEW = "PERM_SEC_SESSIONS_VIEW";
}
