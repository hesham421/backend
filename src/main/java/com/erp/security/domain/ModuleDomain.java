package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.Module;
import com.erp.security.exception.SecErrorCodes;

/**
 * Domain companion for ENTITY-SEC-010 (Module). Owns every "is this operation allowed?"
 * decision for the entity — RULE-SEC-010 (moduleCode uniqueness) and required-field
 * validation. Module is the Tier-1 grantable unit (RULE-SEC-013). No Spring/JPA annotations,
 * no repository access; constructed only via the static factories.
 */
public final class ModuleDomain {

    private final String moduleCode;
    private final boolean active;

    private ModuleDomain(String moduleCode, boolean active) {
        this.moduleCode = moduleCode;
        this.active = active;
    }

    /**
     * Construction-time validation for create: required fields then RULE-SEC-010
     * (moduleCode uniqueness, pre-checked by the service).
     */
    public static ModuleDomain create(String moduleCode, String nameAr, String nameEn, boolean moduleCodeAlreadyTaken) {
        if (isBlank(moduleCode) || isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.MODULE_FIELDS_REQUIRED);
        }
        if (moduleCodeAlreadyTaken) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecErrorCodes.MODULE_CODE_DUPLICATE, moduleCode);
        }
        return new ModuleDomain(moduleCode, true);
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static ModuleDomain from(Module entity) {
        return new ModuleDomain(entity.getModuleCode(), Boolean.TRUE.equals(entity.getIsActive()));
    }

    /** Required-field validation for UPDATE — called before the service mutates the entity. */
    public void assertCanUpdate(String nameAr, String nameEn) {
        if (isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.MODULE_FIELDS_REQUIRED);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public boolean isActive() {
        return active;
    }
}
