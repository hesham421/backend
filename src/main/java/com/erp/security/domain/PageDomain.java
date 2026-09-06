package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.Page;
import com.erp.security.exception.SecErrorCodes;

/**
 * Domain companion for ENTITY-SEC-004 (Page). Owns every "is this operation allowed?"
 * decision for the entity — RULE-SEC-010 (pageCode uniqueness) and required-field validation
 * including the mandatory owning module (moduleFk NOT NULL), which is the basis of the
 * RULE-SEC-014 derivation. The 4-per-page permission generation (RULE-SEC-011) is a
 * multi-entity concern and lives in PermissionGenerationDomainService. No Spring/JPA
 * annotations, no repository access; constructed only via the static factories.
 */
public final class PageDomain {

    private final String pageCode;
    private final boolean active;

    private PageDomain(String pageCode, boolean active) {
        this.pageCode = pageCode;
        this.active = active;
    }

    /**
     * Construction-time validation for create: required fields (including the mandatory
     * moduleFk) then RULE-SEC-010 (pageCode uniqueness, pre-checked by the service).
     */
    public static PageDomain create(String pageCode, String nameAr, String nameEn,
                                    Long moduleFk, boolean pageCodeAlreadyTaken) {
        if (isBlank(pageCode) || isBlank(nameAr) || isBlank(nameEn) || moduleFk == null) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.PAGE_FIELDS_REQUIRED);
        }
        if (pageCodeAlreadyTaken) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecErrorCodes.PAGE_CODE_DUPLICATE, pageCode);
        }
        return new PageDomain(pageCode, true);
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static PageDomain from(Page entity) {
        return new PageDomain(entity.getPageCode(), Boolean.TRUE.equals(entity.getIsActive()));
    }

    /**
     * Required-field validation for UPDATE — called before the service mutates the entity. The owning
     * module and parent page are immutable after creation (structurally enforced by their omission
     * from PageUpdateRequest), so only the mutable display names are validated here.
     */
    public void assertCanUpdate(String nameAr, String nameEn) {
        if (isBlank(nameAr) || isBlank(nameEn)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.PAGE_FIELDS_REQUIRED);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String getPageCode() {
        return pageCode;
    }

    public boolean isActive() {
        return active;
    }
}
