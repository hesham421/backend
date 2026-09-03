package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.Role;
import com.erp.security.exception.SecErrorCodes;

/**
 * Domain companion for ENTITY-SEC-002 (Role). Owns every "is this operation allowed?"
 * decision for the entity — RULE-SEC-010 (roleCode uniqueness) and required-field validation.
 * No Spring/JPA annotations, no repository access; constructed only via the static factories.
 * The service supplies the roleCode-already-taken existence fact — this class never touches
 * persistence.
 */
public final class RoleDomain {

    private final String roleCode;
    private final boolean active;

    private RoleDomain(String roleCode, boolean active) {
        this.roleCode = roleCode;
        this.active = active;
    }

    /**
     * Construction-time validation for create: required fields then RULE-SEC-010
     * (roleCode uniqueness, pre-checked by the service).
     */
    public static RoleDomain create(String roleCode, String nameAr, String nameEn, boolean roleCodeAlreadyTaken) {
        if (isBlank(roleCode) || isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.ROLE_FIELDS_REQUIRED);
        }
        if (roleCodeAlreadyTaken) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecErrorCodes.ROLE_CODE_DUPLICATE, roleCode);
        }
        return new RoleDomain(roleCode, true);
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static RoleDomain from(Role entity) {
        return new RoleDomain(entity.getRoleCode(), Boolean.TRUE.equals(entity.getIsActive()));
    }

    /** Required-field validation for UPDATE — called before the service mutates the entity. */
    public void assertCanUpdate(String nameAr, String nameEn) {
        if (isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.ROLE_FIELDS_REQUIRED);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String getRoleCode() {
        return roleCode;
    }

    public boolean isActive() {
        return active;
    }
}
