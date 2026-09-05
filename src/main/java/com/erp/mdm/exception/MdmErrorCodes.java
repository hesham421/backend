package com.erp.mdm.exception;

/**
 * Module-specific error codes for Master Data (MDM).
 * Descriptive &lt;ENTITY&gt;_&lt;SCENARIO&gt; format (never the numbered ERR-xxxx tracking id).
 * Only the Domain-layer codes (LookupTypeDomain / LookupValueDomain) are declared in this
 * DATA-DOM sub; later phases (SVC-API) append 404 / immutability codes and register bundles.
 */
public final class MdmErrorCodes {

    private MdmErrorCodes() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    /** RULE-MDM-005 (ERR-0002 / ERR-0004) — nameAr/nameEn missing on LookupType create/update. */
    public static final String LOOKUP_TYPE_FIELDS_REQUIRED = "LOOKUP_TYPE_FIELDS_REQUIRED";

    /** RULE-MDM-001 (ERR-0001) — duplicate typeCode on create. */
    public static final String LOOKUP_TYPE_CODE_DUPLICATE = "LOOKUP_TYPE_CODE_DUPLICATE";

    /** RULE-MDM-006 (ERR-0005) — deactivate blocked: type still has active values. */
    public static final String LOOKUP_TYPE_HAS_ACTIVE_VALUES = "LOOKUP_TYPE_HAS_ACTIVE_VALUES";

    /** RULE-MDM-005 (ERR-0009 / ERR-0011) — nameAr/nameEn missing on LookupValue create/update. */
    public static final String LOOKUP_VALUE_FIELDS_REQUIRED = "LOOKUP_VALUE_FIELDS_REQUIRED";

    /** RULE-MDM-003 (ERR-0008) — duplicate valueCode within the same type on create. */
    public static final String LOOKUP_VALUE_CODE_DUPLICATE = "LOOKUP_VALUE_CODE_DUPLICATE";

    /** PLATFORM-STD (ERR-0006) — LookupType not found by id (404) on update/deactivate/getById. */
    public static final String LOOKUP_TYPE_NOT_FOUND = "LOOKUP_TYPE_NOT_FOUND";

    /** PLATFORM-STD (ERR-0012) — LookupValue not found by id (404) on update/deactivate/getById. */
    public static final String LOOKUP_VALUE_NOT_FOUND = "LOOKUP_VALUE_NOT_FOUND";

    /** PLATFORM-STD (ERR-0007) — parent LookupType not found by typeId (404) on value create/list. */
    public static final String LOOKUP_TYPE_PARENT_NOT_FOUND = "LOOKUP_TYPE_PARENT_NOT_FOUND";
}
