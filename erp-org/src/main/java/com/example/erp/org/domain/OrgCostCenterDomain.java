package com.example.erp.org.domain;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.entity.OrgCostCenter;
import com.example.erp.org.exception.OrgErrorCodes;

import java.util.Set;

/**
 * Business Rule owner for {@link OrgCostCenter}. Plain class — no Spring/JPA, no Repository
 * access. Constructed only via {@code create()}/{@code from()}. See {@code domain-layer.md}.
 */
public final class OrgCostCenterDomain {

    private final String costCenterCode;
    private final boolean active;

    private OrgCostCenterDomain(String costCenterCode, boolean active) {
        this.costCenterCode = costCenterCode;
        this.active = active;
    }

    /**
     * Construction-time validation for a new CostCenter. {@code parentBranchActive} is fetched
     * by the Service before this call — the Domain does not touch the Repository (RULE-ORG-019).
     */
    public static OrgCostCenterDomain create(String generatedCode, boolean parentBranchActive) {
        if (generatedCode == null || generatedCode.isBlank()) {
            throw new LocalizedException(Status.VALIDATION_ERROR, "validation.required", "costCenterCode");
        }
        if (!parentBranchActive) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.BR_INACTIVE);
        }
        return new OrgCostCenterDomain(generatedCode, true);
    }

    public static OrgCostCenterDomain from(OrgCostCenter entity) {
        return new OrgCostCenterDomain(entity.getCostCenterCode(), Boolean.TRUE.equals(entity.getIsActiveFl()));
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * RULE-ORG-008 — decision only, mirrors RULE-ORG-007. {@code ancestorIds} is the proposed
     * parent's own ancestor-chain PKs (including itself), fetched by the Service before this
     * call — the Domain does not walk the tree itself.
     */
    public void assertNoCycle(Long selfId, Long proposedParentId, Set<Long> ancestorIds) {
        if (selfId == null || proposedParentId == null) {
            return;
        }
        if (selfId.equals(proposedParentId) || ancestorIds.contains(selfId)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.CC_CYCLE_DETECTED);
        }
    }
}
