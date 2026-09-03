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
}
