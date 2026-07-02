package com.example.erp.org.mapper;

import com.example.erp.org.dto.CostCenterCreateRequest;
import com.example.erp.org.dto.CostCenterResponse;
import com.example.erp.org.dto.CostCenterUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgCostCenter;
import org.springframework.stereotype.Component;

@Component
public class CostCenterMapper {

    public OrgCostCenter toEntity(CostCenterCreateRequest request, String generatedCode, OrgBranch parent, OrgCostCenter parentCostCenter) {
        if (request == null) return null;
        return OrgCostCenter.builder()
                .costCenterCode(generatedCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .branch(parent)
                .parentCostCenter(parentCostCenter)
                .nodeTypeId(request.getNodeTypeId())
                .costCenterTypeId(request.getCostCenterTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    /** Parent reassignment (cycle-checked by the Service/Domain) is applied separately via {@code entity.setParentCostCenter(...)}. */
    public void updateEntityFromRequest(OrgCostCenter entity, CostCenterUpdateRequest request) {
        if (entity == null || request == null) return;
        // ❌ NEVER: entity.setCostCenterCode(...) — IMMUTABLE (RULE-ORG-011/014)
        // ❌ NEVER: entity.setBranch(...) — immutable, not part of update contract
        // ❌ NEVER: entity.setNodeTypeId(...) — IMMUTABLE (RULE-ORG-020)
        if (request.getNameAr() != null) {
            entity.setNameAr(request.getNameAr());
        }
        if (request.getNameEn() != null) {
            entity.setNameEn(request.getNameEn());
        }
        if (request.getCostCenterTypeId() != null) {
            entity.setCostCenterTypeId(request.getCostCenterTypeId());
        }
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
    }

    public CostCenterResponse toResponse(OrgCostCenter entity) {
        if (entity == null) return null;
        OrgBranch branch = entity.getBranch();
        OrgCostCenter parent = entity.getParentCostCenter();
        return CostCenterResponse.builder()
                .id(entity.getId())
                .costCenterCode(entity.getCostCenterCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchFk(branch != null ? branch.getId() : null)
                .parentCostCenterFk(parent != null ? parent.getId() : null)
                .nodeTypeId(entity.getNodeTypeId())
                .costCenterTypeId(entity.getCostCenterTypeId())
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
