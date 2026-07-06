package com.example.erp.org.domain;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.entity.OrgDepartment;
import com.example.erp.org.exception.OrgErrorCodes;

import java.util.Set;

/**
 * Business Rule owner for {@link OrgDepartment}. Plain class — no Spring/JPA, no Repository
 * access. Constructed only via {@code create()}/{@code from()}. See {@code domain-layer.md}.
 */
public final class OrgDepartmentDomain {

    private final String departmentCode;
    private final boolean active;

    private OrgDepartmentDomain(String departmentCode, boolean active) {
        this.departmentCode = departmentCode;
        this.active = active;
    }

    /**
     * Construction-time validation for a new Department. {@code parentBranchActive} is fetched
     * by the Service before this call — the Domain does not touch the Repository (RULE-ORG-019).
     */
    public static OrgDepartmentDomain create(String generatedCode, boolean parentBranchActive) {
        if (generatedCode == null || generatedCode.isBlank()) {
            throw new LocalizedException(Status.VALIDATION_ERROR, "validation.required", "departmentCode");
        }
        if (!parentBranchActive) {
            throw new LocalizedException(Status.PRECONDITION_VIOLATION, OrgErrorCodes.BR_INACTIVE);
        }
        return new OrgDepartmentDomain(generatedCode, true);
    }

    public static OrgDepartmentDomain from(OrgDepartment entity) {
        return new OrgDepartmentDomain(entity.getDepartmentCode(), Boolean.TRUE.equals(entity.getIsActiveFl()));
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * RULE-ORG-007 — decision only. {@code ancestorIds} is the proposed parent's own ancestor-
     * chain PKs (including itself), fetched by the Service before this call — the Domain does not
     * walk the tree itself.
     */
    public void assertNoCycle(Long selfId, Long proposedParentId, Set<Long> ancestorIds) {
        if (selfId == null || proposedParentId == null) {
            return;
        }
        if (selfId.equals(proposedParentId) || ancestorIds.contains(selfId)) {
            throw new LocalizedException(Status.PRECONDITION_VIOLATION, OrgErrorCodes.DEP_CYCLE_DETECTED);
        }
    }
}
