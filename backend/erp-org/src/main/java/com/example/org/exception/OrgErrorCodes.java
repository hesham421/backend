package com.example.org.exception;

public final class OrgErrorCodes {

    private OrgErrorCodes() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ERR-ORG-0001 — RULE-ORG-015 — Name already exists within scope (HTTP 400)
    public static final String NAME_DUPLICATE = "ERR_ORG_0001";

    // ERR-ORG-0002 — RULE-ORG-012 — Business Code generation sequence conflict (HTTP 409)
    public static final String CODE_CONFLICT = "ERR_ORG_0002";

    // ERR-ORG-0003 — RULE-ORG-014/016 — Business Code or audit fields present in payload (HTTP 400)
    public static final String READONLY_FIELD_IN_PAYLOAD = "ERR_ORG_0003";

    // ERR-ORG-0004 — Record not found — LocalizedException(Status.NOT_FOUND, ...) ONLY; NotFoundException BANNED (DRV-ORG-002) (HTTP 404)
    public static final String NOT_FOUND = "ERR_ORG_0004";

    // ERR-ORG-0005 — RULE-ORG-001 — LegalEntity deactivate blocked: active Branches exist (HTTP 409)
    public static final String LE_HAS_ACTIVE_BRANCHES = "ERR_ORG_0005";

    // ERR-ORG-0006 — RULE-ORG-002 — LegalEntity deactivate blocked: active ProfitCenters exist (HTTP 409)
    public static final String LE_HAS_ACTIVE_PROFIT_CENTERS = "ERR_ORG_0006";

    // ERR-ORG-0007 — RULE-ORG-003 — Branch deactivate blocked: active Departments exist (HTTP 409)
    public static final String BR_HAS_ACTIVE_DEPARTMENTS = "ERR_ORG_0007";

    // ERR-ORG-0008 — RULE-ORG-004 — Branch deactivate blocked: active CostCenters exist (HTTP 409)
    public static final String BR_HAS_ACTIVE_COST_CENTERS = "ERR_ORG_0008";

    // ERR-ORG-0009 — RULE-ORG-005 — Branch deactivate blocked: active LocationSites exist (HTTP 409)
    public static final String BR_HAS_ACTIVE_LOCATION_SITES = "ERR_ORG_0009";

    // ERR-ORG-0010 — RULE-ORG-006 — Region deactivate blocked: active Branches reference it (HTTP 409)
    public static final String RG_HAS_ACTIVE_BRANCHES = "ERR_ORG_0010";

    // ERR-ORG-0011 — RULE-ORG-007 — Department parent assignment creates circular reference (HTTP 400)
    public static final String DEPT_CIRCULAR_REFERENCE = "ERR_ORG_0011";

    // ERR-ORG-0012 — RULE-ORG-008 — CostCenter parent assignment creates circular reference (HTTP 400)
    public static final String CC_CIRCULAR_REFERENCE = "ERR_ORG_0012";

    // ERR-ORG-0013 — RULE-ORG-011 — Business Code is immutable after first save (HTTP 400)
    public static final String CODE_IMMUTABLE = "ERR_ORG_0013";

    // ERR-ORG-0014 — RULE-ORG-017 — Region deactivate soft-read consumer warning (HTTP 200 — non-blocking informational)
    public static final String RG_SOFT_READ_WARNING = "ERR_ORG_0014";

    // ERR-ORG-0015 — RULE-ORG-018 — Cannot create Branch under inactive LegalEntity (HTTP 400)
    public static final String INACTIVE_LEGAL_ENTITY = "ERR_ORG_0015";

    // ERR-ORG-0016 — RULE-ORG-019 — Cannot create org unit under inactive Branch (HTTP 400)
    public static final String INACTIVE_BRANCH = "ERR_ORG_0016";

    // ERR-ORG-0017 — RULE-ORG-020 — node_type_id (SUMMARY/DETAIL) is immutable after first save (HTTP 400)
    public static final String NODE_TYPE_IMMUTABLE = "ERR_ORG_0017";

    // ERR-ORG-0018 — RULE-ORG-009/010 — SUMMARY node cannot be used on transactional records (HTTP 400)
    public static final String SUMMARY_NODE_ON_TRANSACTIONAL = "ERR_ORG_0018";
}
