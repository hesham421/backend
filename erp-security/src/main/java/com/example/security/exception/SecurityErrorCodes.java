package com.example.security.exception;

/**
 * Centralized Error Codes for Security Module
 * 
 * Rule 31.3: Error codes must follow UPPERCASE_SNAKE_CASE format
 * Pattern: SEC_<ENTITY>_<ERROR_DESCRIPTION>
 * 
 * All codes must have corresponding messages in:
 * - erp-main/src/main/resources/i18n/messages.properties (English)
 * - erp-main/src/main/resources/i18n/messages_ar.properties (Arabic)
 */
public final class SecurityErrorCodes {

    private SecurityErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== User Errors ====================
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_ENTITY_NOT_FOUND = "USER_ENTITY_NOT_FOUND";
    public static final String USERNAME_ALREADY_EXISTS = "USERNAME_ALREADY_EXISTS";
    public static final String USER_HAS_ACTIVE_REFRESH_TOKENS = "USER_HAS_ACTIVE_REFRESH_TOKENS";
    public static final String USER_HAS_DEPENDENCIES = "USER_HAS_DEPENDENCIES";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

    // ==================== Role Errors ====================
    public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";
    public static final String ROLE_ALREADY_EXISTS = "ROLE_ALREADY_EXISTS";
    public static final String DUPLICATE_ROLE_CODE = "DUPLICATE_ROLE_CODE";
    public static final String DUPLICATE_ROLE_NAME = "DUPLICATE_ROLE_NAME";
    public static final String ROLE_IN_USE = "ROLE_IN_USE";

    // ==================== Permission Errors ====================
    public static final String PERMISSION_NOT_FOUND = "PERMISSION_NOT_FOUND";
    public static final String PERMISSION_ALREADY_EXISTS = "PERMISSION_ALREADY_EXISTS";
    public static final String PERMISSION_NOT_ASSIGNED_TO_ROLE = "PERMISSION_NOT_ASSIGNED_TO_ROLE";
    public static final String PERMISSIONS_NOT_FOUND = "PERMISSIONS_NOT_FOUND";
    public static final String INVALID_PERMISSION_TYPE = "INVALID_PERMISSION_TYPE";

    // ==================== Page Errors ====================
    public static final String PAGE_NOT_FOUND = "PAGE_NOT_FOUND";
    public static final String PAGE_NOT_FOUND_BY_CODE = "PAGE_NOT_FOUND_BY_CODE";
    public static final String DUPLICATE_PAGE_CODE = "DUPLICATE_PAGE_CODE";
    public static final String DUPLICATE_ROUTE = "DUPLICATE_ROUTE";
    public static final String INVALID_PAGE_CODE_FORMAT = "INVALID_PAGE_CODE_FORMAT";
    public static final String INVALID_PAGE_CODE_LENGTH = "INVALID_PAGE_CODE_LENGTH";
    public static final String INVALID_ROUTE_FORMAT = "INVALID_ROUTE_FORMAT";
    public static final String PARENT_PAGE_NOT_FOUND = "PARENT_PAGE_NOT_FOUND";
    public static final String INVALID_PARENT_PAGE = "INVALID_PARENT_PAGE";
    public static final String CANNOT_REMOVE_VIEW_PERMISSION = "CANNOT_REMOVE_VIEW_PERMISSION";
    public static final String PAGE_ALREADY_ASSIGNED_TO_ROLE = "PAGE_ALREADY_ASSIGNED_TO_ROLE";
    public static final String PAGE_NOT_ASSIGNED_TO_ROLE = "PAGE_NOT_ASSIGNED_TO_ROLE";

    // ==================== Token / Authentication Errors ====================
    public static final String NO_REFRESH_COOKIE = "NO_REFRESH_COOKIE";
    public static final String REFRESH_REVOKED = "REFRESH_REVOKED";
    public static final String REFRESH_EXPIRED_OR_REVOKED = "REFRESH_EXPIRED_OR_REVOKED";
    public static final String RATE_LIMIT_LOGIN_EXCEEDED = "RATE_LIMIT_LOGIN_EXCEEDED";

    // ==================== Tenant Errors ====================

    // ==================== General Operation Errors ====================
    public static final String INVALID_OPERATION = "INVALID_OPERATION";
    public static final String NO_PERMISSIONS_TO_COPY = "NO_PERMISSIONS_TO_COPY";
    public static final String DB_CONSTRAINT_VIOLATION = "DB_CONSTRAINT_VIOLATION";

    // ==================== DataScope / Self-Service Auth Errors (PLAN-SEC-002, Phase SVC+API) ====================
    // Baseline CRUD errors (not bound to a specific RULE-ID / ERR-ID in
    // execution-plan-SEC-gaps.md Section 4.2 — that catalog only lists new gap-specific
    // rule-derived errors, same convention as USER_NOT_FOUND/ROLE_NOT_FOUND above).
    public static final String SEC_USER_PROFILE_NOT_FOUND = "SEC_USER_PROFILE_NOT_FOUND";
    public static final String SEC_USER_PROFILE_ALREADY_EXISTS = "SEC_USER_PROFILE_ALREADY_EXISTS";
    public static final String SEC_ROLE_BRANCH_NOT_FOUND = "SEC_ROLE_BRANCH_NOT_FOUND";

    // Bound to execution-plan-SEC-gaps.md Section 4.2 ERR-IDs (message text copied verbatim
    // into messages.properties / messages_ar.properties):
    public static final String SEC_USER_PROFILE_BRANCH_INACTIVE = "SEC_USER_PROFILE_BRANCH_INACTIVE"; // ERR-SEC-1034 (RULE-SEC-034)
    public static final String SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED = "SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED"; // ERR-SEC-1035 (RULE-SEC-035)
    public static final String SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT = "SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT"; // ERR-SEC-1036 (RULE-SEC-036)
    public static final String ACTIVATION_TOKEN_INVALID_OR_EXPIRED = "ACTIVATION_TOKEN_INVALID_OR_EXPIRED"; // ERR-SEC-1032 (RULE-SEC-032)
    public static final String TOKEN_ALREADY_USED = "TOKEN_ALREADY_USED"; // ERR-SEC-1033 (RULE-SEC-033, shared activation/reset wording)
    public static final String RESET_TOKEN_INVALID_OR_EXPIRED = "RESET_TOKEN_INVALID_OR_EXPIRED"; // ERR-SEC-1043 (RULE-SEC-033, reset variant)
    // Deliberately NOT reusing USERNAME_ALREADY_EXISTS above: that constant's existing message
    // ("Username already exists in tenant: {0}") is bound to the admin-facing CreateUserRequest
    // flow and must not be overwritten; ERR-SEC-1040's plan-mandated text differs verbatim.
    public static final String SIGNUP_USERNAME_ALREADY_EXISTS = "SIGNUP_USERNAME_ALREADY_EXISTS"; // ERR-SEC-1040 (RULE-SEC-040)
    public static final String SIGNUP_EMAIL_ALREADY_EXISTS = "SIGNUP_EMAIL_ALREADY_EXISTS"; // ERR-SEC-1041 (RULE-SEC-041)
}
