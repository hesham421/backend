package com.example.erp.org.domain;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.entity.OrgRegion;

/**
 * Business Rule owner for {@link OrgRegion}. Plain class — no Spring/JPA, no Repository access.
 * Constructed only via {@code create()}/{@code from()}. See {@code domain-layer.md}.
 *
 * TODO: OQ-001 — RULE-ORG-006 (block deactivation while active Branches reference this Region)
 * is pending resolution: db-script.md's DBF matrix has no REGION_FK column on ORG_BRANCH, so the
 * guard cannot be implemented against the current schema. Not enforced here until the FK linkage
 * is confirmed at a MODE 1.5 amendment. RULE-ORG-017 (SOFT-READ consumer warning) is non-blocking
 * and surfaced at the UI layer only — no Domain guard required.
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
