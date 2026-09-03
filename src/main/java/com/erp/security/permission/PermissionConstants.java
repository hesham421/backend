package com.erp.security.permission;

/**
 * PLACEHOLDER — the SECURITY module owns the real permission-constants class and has not been
 * built yet. This carries only the authorities modules built before SECURITY actually need,
 * added on demand as each module's SVC-API phase requires them. Every {@code @PreAuthorize} in
 * the codebase references this class so no module hardcodes a permission string (see
 * gov-enforce-backend-contract A.5.2). Replace/reconcile with SECURITY's own permission registry
 * when that module's CORE phase runs — do not let this linger as two sources of truth.
 */
public final class PermissionConstants {

    private PermissionConstants() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    // --- Common Utils (CU) — ENTITY-CU-001 AppConfiguration ---------------
    public static final String CONFIG_VIEW = "CONFIG_VIEW";
    public static final String CONFIG_CREATE = "CONFIG_CREATE";
    public static final String CONFIG_UPDATE = "CONFIG_UPDATE";
    public static final String CONFIG_DEACTIVATE = "CONFIG_DEACTIVATE";

    // --- Security (SEC) — SCR-SEC-004 Module Registry (ENTITY-SEC-010) -----
    // CORE-9 convention PERM_<PAGE_CODE>_<TYPE>; SEC-BE seeds matching SEC_PERMISSION rows.
    public static final String PERM_SEC_MODULES_VIEW = "PERM_SEC_MODULES_VIEW";
    public static final String PERM_SEC_MODULES_CREATE = "PERM_SEC_MODULES_CREATE";
    public static final String PERM_SEC_MODULES_UPDATE = "PERM_SEC_MODULES_UPDATE";
    public static final String PERM_SEC_MODULES_DELETE = "PERM_SEC_MODULES_DELETE";

    // --- Security (SEC) — SCR-SEC-002 Roles·Modules·Perms (ENTITY-SEC-002) -
    // Tier-1 assign/revoke module→role (API-SEC-017/018) reuses PERM_SEC_ROLES_UPDATE; the
    // SVC-API-RBAC sub adds the remaining authorities for Roles CRUD (API-SEC-011), read-only
    // permission listing (API-SEC-014, VIEW) and Tier-2 grant/revoke (API-SEC-015, UPDATE).
    public static final String PERM_SEC_ROLES_VIEW = "PERM_SEC_ROLES_VIEW";
    public static final String PERM_SEC_ROLES_CREATE = "PERM_SEC_ROLES_CREATE";
    public static final String PERM_SEC_ROLES_UPDATE = "PERM_SEC_ROLES_UPDATE";
    public static final String PERM_SEC_ROLES_DELETE = "PERM_SEC_ROLES_DELETE";

    // --- Security (SEC) — SCR-SEC-001 User Management (ENTITY-SEC-001) -----
    // page_code SEC_USERS; SEC-BE seeds matching SEC_PERMISSION rows.
    public static final String PERM_SEC_USERS_VIEW = "PERM_SEC_USERS_VIEW";
    public static final String PERM_SEC_USERS_CREATE = "PERM_SEC_USERS_CREATE";
    public static final String PERM_SEC_USERS_UPDATE = "PERM_SEC_USERS_UPDATE";
    public static final String PERM_SEC_USERS_DELETE = "PERM_SEC_USERS_DELETE";

    // --- Security (SEC) — SCR-SEC-003 Page Registry (ENTITY-SEC-004) -------
    // page_code SEC_PAGE_REGISTRY; SEC-BE seeds matching SEC_PERMISSION rows.
    public static final String PERM_SEC_PAGE_REGISTRY_VIEW = "PERM_SEC_PAGE_REGISTRY_VIEW";
    public static final String PERM_SEC_PAGE_REGISTRY_CREATE = "PERM_SEC_PAGE_REGISTRY_CREATE";
    public static final String PERM_SEC_PAGE_REGISTRY_UPDATE = "PERM_SEC_PAGE_REGISTRY_UPDATE";
    public static final String PERM_SEC_PAGE_REGISTRY_DELETE = "PERM_SEC_PAGE_REGISTRY_DELETE";
}
