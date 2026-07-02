package com.example.erp.org.exception;

/**
 * Centralized Error Codes for Organization Module
 *
 * All codes must have corresponding messages in:
 * - erp-main/src/main/resources/i18n/messages.properties (English)
 * - erp-main/src/main/resources/i18n/messages_ar.properties (Arabic)
 */
public final class OrgErrorCodes {

    private OrgErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Message-key values are the canonical ERR-ORG-IDs from SECTION A (registry-exec-org.md /
    // execution-plan.md ERROR CATALOG), and match the keys already populated in
    // erp-main/src/main/resources/i18n/messages*.properties.

    // ==================== Shared — ALL entities (RULE-ORG-011..016) ====================
    public static final String NAME_DUPLICATE = "ERR_ORG_0001";              // RULE-ORG-015
    public static final String CODE_GENERATION_CONFLICT = "ERR_ORG_0002";    // RULE-ORG-012
    public static final String RECORD_NOT_FOUND = "ERR_ORG_0004";            // generic not-found

    // ==================== LegalEntity Errors (RULE-ORG-001/002) ====================
    public static final String LE_HAS_ACTIVE_BRANCHES = "ERR_ORG_0005";
    public static final String LE_HAS_ACTIVE_PROFIT_CENTERS = "ERR_ORG_0006";
    public static final String LE_INACTIVE = "ERR_ORG_0015";

    // ==================== Branch Errors (RULE-ORG-003/004/005/018) ====================
    public static final String BR_HAS_ACTIVE_DEPARTMENTS = "ERR_ORG_0007";
    public static final String BR_HAS_ACTIVE_COST_CENTERS = "ERR_ORG_0008";
    public static final String BR_HAS_ACTIVE_LOCATION_SITES = "ERR_ORG_0009";

    // ==================== Region Errors (RULE-ORG-006 — OQ-001 DEFERRED, not wired) ====================
    public static final String RG_HAS_ACTIVE_BRANCHES = "ERR_ORG_0010";

    // ==================== Department Errors (RULE-ORG-007) ====================
    public static final String DEP_CYCLE_DETECTED = "ERR_ORG_0011";

    // ==================== CostCenter Errors (RULE-ORG-008) ====================
    public static final String CC_CYCLE_DETECTED = "ERR_ORG_0012";

    // ==================== Shared Creation Guard — Department/CostCenter/LocationSite (RULE-ORG-019) ====================
    public static final String BR_INACTIVE = "ERR_ORG_0016";
}
