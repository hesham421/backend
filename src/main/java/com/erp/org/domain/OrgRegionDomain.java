package com.erp.org.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.org.entity.OrgRegion;

/**
 * TODO: OQ-001 — RULE-ORG-006 (block deactivation while Regions still have active Branches)
 * can't be enforced yet: ORG_BRANCH has no REGION_FK column. RULE-ORG-017 is UI-only; no Domain
 * guard needed.
 */
public final class OrgRegionDomain {

    private final String regionCode;
    private final boolean active;

    private OrgRegionDomain(String regionCode, boolean active) {
        this.regionCode = regionCode;
        this.active = active;
    }

    public static OrgRegionDomain create(String generatedCode) {
        if (generatedCode == null || generatedCode.isBlank()) {
            throw new LocalizedException(Status.VALIDATION_ERROR, "validation.required", "regionCode");
        }
        return new OrgRegionDomain(generatedCode, true);
    }

    public static OrgRegionDomain from(OrgRegion entity) {
        return new OrgRegionDomain(entity.getRegionCode(), Boolean.TRUE.equals(entity.getIsActiveFl()));
    }

    public String getRegionCode() {
        return regionCode;
    }

    public boolean isActive() {
        return active;
    }
}
