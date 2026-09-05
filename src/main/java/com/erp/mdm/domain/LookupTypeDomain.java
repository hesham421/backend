package com.erp.mdm.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.mdm.entity.LookupType;
import com.erp.mdm.exception.MdmErrorCodes;

/**
 * Domain companion for ENTITY-MDM-001 (LookupType). Owns every "is this operation allowed?"
 * decision — RULE-MDM-001 (typeCode uniqueness), RULE-MDM-005 (required names) and RULE-MDM-006
 * (block deactivating a type with active values). typeCode immutability (RULE-MDM-002) is enforced
 * structurally by excluding it from the UpdateRequest DTO. No Spring/JPA annotations, no repository
 * access; constructed only via the static factories.
 */
public final class LookupTypeDomain {

    private LookupTypeDomain() {
    }

    /**
     * Construction-time validation for create: required names (RULE-MDM-005) then RULE-MDM-001
     * (typeCode uniqueness, pre-checked by the service).
     */
    public static LookupTypeDomain create(String typeCode, String nameAr, String nameEn,
                                          boolean typeCodeAlreadyTaken) {
        if (isBlank(typeCode) || isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, MdmErrorCodes.LOOKUP_TYPE_FIELDS_REQUIRED);
        }
        if (typeCodeAlreadyTaken) {
            throw new LocalizedException(Status.ALREADY_EXISTS, MdmErrorCodes.LOOKUP_TYPE_CODE_DUPLICATE, typeCode);
        }
        return new LookupTypeDomain();
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static LookupTypeDomain from(LookupType entity) {
        return new LookupTypeDomain();
    }

    /** Required-name validation for UPDATE (RULE-MDM-005) — called before the service mutates. */
    public void assertCanUpdate(String nameAr, String nameEn) {
        if (isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, MdmErrorCodes.LOOKUP_TYPE_FIELDS_REQUIRED);
        }
    }

    /**
     * RULE-MDM-006 (DRV-008) — block soft-deactivation while active LookupValues still exist.
     * The service supplies the count; a referencing record existing is a CONFLICT (409).
     */
    public void assertCanDeactivate(long activeValueCount) {
        if (activeValueCount > 0) {
            throw new LocalizedException(Status.CONFLICT, MdmErrorCodes.LOOKUP_TYPE_HAS_ACTIVE_VALUES);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
