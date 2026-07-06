package com.example.erp.org.domain;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.entity.OrgLocationSite;
import com.example.erp.org.exception.OrgErrorCodes;

/**
 * Business Rule owner for {@link OrgLocationSite}. Plain class — no Spring/JPA, no Repository
 * access. Constructed only via {@code create()}/{@code from()}. See {@code domain-layer.md}.
 */
public final class OrgLocationSiteDomain {

    private final String locationSiteCode;
    private final boolean active;

    private OrgLocationSiteDomain(String locationSiteCode, boolean active) {
        this.locationSiteCode = locationSiteCode;
        this.active = active;
    }

    /**
     * Construction-time validation for a new LocationSite. {@code parentBranchActive} is fetched
     * by the Service before this call — the Domain does not touch the Repository (RULE-ORG-019).
     */
    public static OrgLocationSiteDomain create(String generatedCode, boolean parentBranchActive) {
        if (generatedCode == null || generatedCode.isBlank()) {
            throw new LocalizedException(Status.VALIDATION_ERROR, "validation.required", "locationSiteCode");
        }
        if (!parentBranchActive) {
            throw new LocalizedException(Status.PRECONDITION_VIOLATION, OrgErrorCodes.BR_INACTIVE);
        }
        return new OrgLocationSiteDomain(generatedCode, true);
    }

    public static OrgLocationSiteDomain from(OrgLocationSite entity) {
        return new OrgLocationSiteDomain(entity.getLocationSiteCode(), Boolean.TRUE.equals(entity.getIsActiveFl()));
    }

    public String getLocationSiteCode() {
        return locationSiteCode;
    }

    public boolean isActive() {
        return active;
    }
}
