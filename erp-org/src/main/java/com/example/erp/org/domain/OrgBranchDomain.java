package com.example.erp.org.domain;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.exception.OrgErrorCodes;

/**
 * Business Rule owner for {@link OrgBranch}. Plain class — no Spring/JPA, no Repository access.
 * Constructed only via {@code create()}/{@code from()}. See {@code domain-layer.md}.
 */
public final class OrgBranchDomain {

    private final String branchCode;
    private final boolean active;

    private OrgBranchDomain(String branchCode, boolean active) {
        this.branchCode = branchCode;
        this.active = active;
    }

    /**
     * Construction-time validation for a new Branch. {@code parentLegalEntityActive} is fetched
     * by the Service before this call — the Domain does not touch the Repository (RULE-ORG-018).
     */
    public static OrgBranchDomain create(String generatedCode, boolean parentLegalEntityActive) {
        if (generatedCode == null || generatedCode.isBlank()) {
            throw new LocalizedException(Status.VALIDATION_ERROR, "validation.required", "branchCode");
        }
        if (!parentLegalEntityActive) {
            throw new LocalizedException(Status.PRECONDITION_VIOLATION, OrgErrorCodes.LE_INACTIVE);
        }
        return new OrgBranchDomain(generatedCode, true);
    }

    public static OrgBranchDomain from(OrgBranch entity) {
        return new OrgBranchDomain(entity.getBranchCode(), Boolean.TRUE.equals(entity.getIsActiveFl()));
    }

    public String getBranchCode() {
        return branchCode;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * RULE-ORG-003 / RULE-ORG-004 / RULE-ORG-005 — decision only; the Service performs
     * {@code entity.deactivate()} after this returns without throwing.
     */
    public void assertCanDeactivate(long activeDepartmentCount, long activeCostCenterCount, long activeLocationSiteCount) {
        if (activeDepartmentCount > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.BR_HAS_ACTIVE_DEPARTMENTS);
        }
        if (activeCostCenterCount > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.BR_HAS_ACTIVE_COST_CENTERS);
        }
        if (activeLocationSiteCount > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.BR_HAS_ACTIVE_LOCATION_SITES);
        }
    }
}
