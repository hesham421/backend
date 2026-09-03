package com.erp.security.exception;

/**
 * Module-specific error codes for Security (SEC).
 * Descriptive &lt;ENTITY&gt;_&lt;SCENARIO&gt; format (never the numbered ERR-xxxx tracking id).
 * Only the identity (ENTITY-SEC-001 / UserAccountDomain) codes are declared in this DATA-DOM
 * sub; later phases (RBAC, TOKENS, DOC error-catalog) append their own and register the
 * message bundles.
 */
public final class SecErrorCodes {

    private SecErrorCodes() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /** RULE-SEC-002 — username/email/fullName missing on create. */
    public static final String USER_ACCOUNT_FIELDS_REQUIRED = "USER_ACCOUNT_FIELDS_REQUIRED";

    /** RULE-SEC-001 — duplicate username on create. */
    public static final String USER_ACCOUNT_USERNAME_DUPLICATE = "USER_ACCOUNT_USERNAME_DUPLICATE";

    /** RULE-SEC-001 — duplicate email on create. */
    public static final String USER_ACCOUNT_EMAIL_DUPLICATE = "USER_ACCOUNT_EMAIL_DUPLICATE";

    /** RULE-SEC-003 — password does not meet complexity policy. */
    public static final String USER_ACCOUNT_PASSWORD_COMPLEXITY = "USER_ACCOUNT_PASSWORD_COMPLEXITY";

    /** RULE-SEC-004 — attempt to store a non-hashed password. */
    public static final String USER_ACCOUNT_PASSWORD_NOT_HASHED = "USER_ACCOUNT_PASSWORD_NOT_HASHED";

    /** RULE-SEC-009 — login attempted while userStatusId != ACTIVE. */
    public static final String USER_ACCOUNT_LOGIN_NOT_ACTIVE = "USER_ACCOUNT_LOGIN_NOT_ACTIVE";

    /** ERR-0011 (PLATFORM-STD, DRV-001) — username unknown or password mismatch on login. */
    public static final String USER_ACCOUNT_INVALID_CREDENTIALS = "USER_ACCOUNT_INVALID_CREDENTIALS";

    /** RULE-SEC-005 — login attempted while the account is temporarily locked. */
    public static final String USER_ACCOUNT_LOCKED = "USER_ACCOUNT_LOCKED";

    /** RULE-SEC-012 — invalid userStatusId lifecycle transition. */
    public static final String USER_ACCOUNT_INVALID_STATUS_TRANSITION = "USER_ACCOUNT_INVALID_STATUS_TRANSITION";

    /** ERR-0012 (PLATFORM-STD, DRV-002) — user account addressed by id was not found (API-SEC-009/010/012). */
    public static final String USER_ACCOUNT_NOT_FOUND = "USER_ACCOUNT_NOT_FOUND";

    /** RULE-SEC-002 pattern — roleCode/nameAr/nameEn missing on create/update. */
    public static final String ROLE_FIELDS_REQUIRED = "ROLE_FIELDS_REQUIRED";

    /** RULE-SEC-010 — duplicate roleCode. */
    public static final String ROLE_CODE_DUPLICATE = "ROLE_CODE_DUPLICATE";

    /** RULE-SEC-002 pattern — moduleCode/nameAr/nameEn missing on create/update. */
    public static final String MODULE_FIELDS_REQUIRED = "MODULE_FIELDS_REQUIRED";

    /** RULE-SEC-010 — duplicate moduleCode. */
    public static final String MODULE_CODE_DUPLICATE = "MODULE_CODE_DUPLICATE";

    /** ERR-0012 (PLATFORM-STD, DRV-002) — module addressed by id was not found (API-SEC-020/017/018). */
    public static final String MODULE_NOT_FOUND = "MODULE_NOT_FOUND";

    /** ERR-0012 (PLATFORM-STD, DRV-002) — role addressed by id was not found (API-SEC-017/018). */
    public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";

    /** RULE-SEC-002 pattern — pageCode/nameAr/nameEn/moduleFk missing on create/update. */
    public static final String PAGE_FIELDS_REQUIRED = "PAGE_FIELDS_REQUIRED";

    /** RULE-SEC-010 — duplicate pageCode. */
    public static final String PAGE_CODE_DUPLICATE = "PAGE_CODE_DUPLICATE";

    /** ERR-0012 (PLATFORM-STD, DRV-002) — page (or parent page) addressed by id was not found (API-SEC-013). */
    public static final String PAGE_NOT_FOUND = "PAGE_NOT_FOUND";

    /** ERR-0012 (PLATFORM-STD, DRV-002) — permission addressed by id was not found (API-SEC-015). */
    public static final String PERMISSION_NOT_FOUND = "PERMISSION_NOT_FOUND";

    /** RULE-SEC-011 — a generated permissionCode already exists (generation collision). */
    public static final String PERMISSION_CODE_DUPLICATE = "PERMISSION_CODE_DUPLICATE";

    /** RULE-SEC-014 — screen permission grant blocked: role does not hold the page's module. */
    public static final String ROLE_PERMISSION_MODULE_NOT_GRANTED = "ROLE_PERMISSION_MODULE_NOT_GRANTED";

    /** RULE-SEC-014 — module revoke blocked: role still holds screen permissions in that module. */
    public static final String ROLE_MODULE_REVOKE_HAS_DEPENDENTS = "ROLE_MODULE_REVOKE_HAS_DEPENDENTS";

    /** RULE-SEC-006 — refresh token presented for rotation is expired. */
    public static final String REFRESH_TOKEN_EXPIRED = "REFRESH_TOKEN_EXPIRED";

    /** RULE-SEC-006 — refresh token presented for rotation is already revoked. */
    public static final String REFRESH_TOKEN_REVOKED = "REFRESH_TOKEN_REVOKED";

    /** RULE-SEC-007 — password reset token presented for consumption is expired. */
    public static final String PASSWORD_RESET_TOKEN_EXPIRED = "PASSWORD_RESET_TOKEN_EXPIRED";

    /** RULE-SEC-007 — password reset token has already been used (single-use). */
    public static final String PASSWORD_RESET_TOKEN_USED = "PASSWORD_RESET_TOKEN_USED";

    /** RULE-SEC-008 — account activation token presented for consumption is expired. */
    public static final String ACCOUNT_ACTIVATION_TOKEN_EXPIRED = "ACCOUNT_ACTIVATION_TOKEN_EXPIRED";

    /** RULE-SEC-008 — account activation token has already been used (single-use). */
    public static final String ACCOUNT_ACTIVATION_TOKEN_USED = "ACCOUNT_ACTIVATION_TOKEN_USED";

    /** ERR-0012 (PLATFORM-STD, DRV-002) — API-SEC-016 lookup requested for an unknown lookupKey. */
    public static final String LOOKUP_KEY_NOT_FOUND = "LOOKUP_KEY_NOT_FOUND";
}
