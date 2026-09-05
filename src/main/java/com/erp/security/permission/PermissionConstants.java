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

    // --- Master Data (MDM) — SCR-MDM-001 Reference Data Lookup Mgmt (ENTITY-MDM-001) -
    // page_code MDM_LOOKUP; SEC-BE seeds matching SEC_PERMISSION rows.
    public static final String PERM_MDM_LOOKUP_VIEW = "PERM_MDM_LOOKUP_VIEW";
    public static final String PERM_MDM_LOOKUP_CREATE = "PERM_MDM_LOOKUP_CREATE";
    public static final String PERM_MDM_LOOKUP_UPDATE = "PERM_MDM_LOOKUP_UPDATE";
    public static final String PERM_MDM_LOOKUP_DELETE = "PERM_MDM_LOOKUP_DELETE";

    // --- Notification Service (NOTIF) — SCR-NOTIF-001 Templates (ENTITY-NOTIF-002) ---
    // page_code NOTIF_TEMPLATES; codes follow PERM_<PAGE_CODE>_<TYPE> so they equal the V7-seeded
    // SEC_PERMISSION rows and runtime PermissionGenerationDomainService output. (SEC-BE renamed these
    // from the earlier SINGULAR PERM_NOTIF_TEMPLATE_* to the PLURAL, page-code-derived form.)
    public static final String PERM_NOTIF_TEMPLATES_VIEW = "PERM_NOTIF_TEMPLATES_VIEW";
    public static final String PERM_NOTIF_TEMPLATES_CREATE = "PERM_NOTIF_TEMPLATES_CREATE";
    public static final String PERM_NOTIF_TEMPLATES_UPDATE = "PERM_NOTIF_TEMPLATES_UPDATE";
    public static final String PERM_NOTIF_TEMPLATES_DELETE = "PERM_NOTIF_TEMPLATES_DELETE";

    // --- Notification Service (NOTIF) — SCR-NOTIF-002 Channel Config (ENTITY-NOTIF-003) ---
    // page_code NOTIF_CHANNELS; PERM_<PAGE_CODE>_<TYPE> (SEC-BE renamed from SINGULAR
    // PERM_NOTIF_CHANNEL_* to the PLURAL, page-code-derived form). SEC-BE seeds matching rows (V7).
    public static final String PERM_NOTIF_CHANNELS_VIEW = "PERM_NOTIF_CHANNELS_VIEW";
    public static final String PERM_NOTIF_CHANNELS_CREATE = "PERM_NOTIF_CHANNELS_CREATE";
    public static final String PERM_NOTIF_CHANNELS_UPDATE = "PERM_NOTIF_CHANNELS_UPDATE";
    public static final String PERM_NOTIF_CHANNELS_DELETE = "PERM_NOTIF_CHANNELS_DELETE";

    // --- Notification Service (NOTIF) — SCR-NOTIF-003 Notification Log (ENTITY-NOTIF-001) ---
    // page_code NOTIF_LOG; VIEW-only screen (system record). SEC-BE seeds the SEC_PERMISSION row.
    public static final String PERM_NOTIF_LOG_VIEW = "PERM_NOTIF_LOG_VIEW";

    // --- File Service (FILE) — SCR-FILE-001 File Categories (ENTITY-FILE-002) ---
    // page_code FILE_CATEGORIES; PERM_<PAGE_CODE>_<TYPE> (plural, page-code-derived from the start).
    // SEC-BE seeds matching SEC_PERMISSION rows.
    public static final String PERM_FILE_CATEGORIES_VIEW = "PERM_FILE_CATEGORIES_VIEW";
    public static final String PERM_FILE_CATEGORIES_CREATE = "PERM_FILE_CATEGORIES_CREATE";
    public static final String PERM_FILE_CATEGORIES_UPDATE = "PERM_FILE_CATEGORIES_UPDATE";
    public static final String PERM_FILE_CATEGORIES_DELETE = "PERM_FILE_CATEGORIES_DELETE";

    // --- File Service (FILE) — SCR-FILE-002 File Browser (ENTITY-FILE-001) ---
    // page_code FILE_BROWSER. VIEW guards metadata/list/access-token; CREATE upload; UPDATE archive;
    // DELETE soft-delete. Download (API-FILE-003) is token-gated, not a FILE_BROWSER permission.
    // SEC-BE seeds matching SEC_PERMISSION rows.
    public static final String PERM_FILE_BROWSER_VIEW = "PERM_FILE_BROWSER_VIEW";
    public static final String PERM_FILE_BROWSER_CREATE = "PERM_FILE_BROWSER_CREATE";
    public static final String PERM_FILE_BROWSER_UPDATE = "PERM_FILE_BROWSER_UPDATE";
    public static final String PERM_FILE_BROWSER_DELETE = "PERM_FILE_BROWSER_DELETE";

    // NOTE (NOTIF dispatch, API-NOTIF-001): no dispatch permission constant. Dispatch is a
    // service/event endpoint behind the Security filter (RULE-NOTIF-005), NOT tied to a management
    // screen, and SEC_PERMISSION.PAGE_FK is NOT NULL — so a dispatch permission cannot be seeded.
    // The dispatch gate is @PreAuthorize("isAuthenticated()") in DispatchService (SEC-BE).
}
