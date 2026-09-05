package com.erp.mdm.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.mdm.entity.LookupValue;
import com.erp.mdm.exception.MdmErrorCodes;

/**
 * Domain companion for ENTITY-MDM-002 (LookupValue). Owns every "is this operation allowed?"
 * decision — RULE-MDM-003 (valueCode uniqueness within its type) and RULE-MDM-005 (required
 * names). valueCode immutability (RULE-MDM-004) is enforced structurally by excluding it from the
 * UpdateRequest DTO. LookupValue is a leaf (DRV-008) — no child-usage check. No Spring/JPA
 * annotations, no repository access; constructed only via the static factories.
 */
public final class LookupValueDomain {

    private LookupValueDomain() {
    }

    /**
     * Construction-time validation for create: required names (RULE-MDM-005) then RULE-MDM-003
     * (valueCode uniqueness within the parent type, pre-checked by the service).
     */
    public static LookupValueDomain create(String valueCode, String nameAr, String nameEn,
                                           boolean valueCodeAlreadyTakenInType) {
        if (isBlank(valueCode) || isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, MdmErrorCodes.LOOKUP_VALUE_FIELDS_REQUIRED);
        }
        if (valueCodeAlreadyTakenInType) {
            throw new LocalizedException(Status.ALREADY_EXISTS, MdmErrorCodes.LOOKUP_VALUE_CODE_DUPLICATE, valueCode);
        }
        return new LookupValueDomain();
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static LookupValueDomain from(LookupValue entity) {
        return new LookupValueDomain();
    }

    /** Required-name validation for UPDATE (RULE-MDM-005) — called before the service mutates. */
    public void assertCanUpdate(String nameAr, String nameEn) {
        if (isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, MdmErrorCodes.LOOKUP_VALUE_FIELDS_REQUIRED);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
