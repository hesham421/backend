package com.example.org.mapper;

import com.example.org.domain.OrgBranch;
import com.example.org.domain.OrgCostCenter;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgCostCenterMapper {

    public OrgCostCenter toEntity(CostCenterCreateRequest request, OrgBranch branch, OrgCostCenter parentCostCenter) {
        if (request == null) return null;
        return OrgCostCenter.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .branch(branch)
                .parentCostCenter(parentCostCenter)
                .nodeTypeId(request.getNodeTypeId())
                .costCenterTypeId(request.getCostCenterTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgCostCenter entity, CostCenterUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setCostCenterTypeId(request.getCostCenterTypeId());
        entity.setNotes(request.getNotes());
        // parentCostCenter FK updated by service after circular-ref check (RULE-ORG-008)
    }

    public CostCenterResponse toResponse(OrgCostCenter entity) {
        if (entity == null) return null;
        return CostCenterResponse.builder()
                .id(entity.getId())
                .costCenterCode(entity.getCostCenterCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .branchNameEn(entity.getBranch() != null ? entity.getBranch().getNameEn() : null)
                .parentId(entity.getParentCostCenter() != null ? entity.getParentCostCenter().getId() : null)
                .parentNameEn(entity.getParentCostCenter() != null ? entity.getParentCostCenter().getNameEn() : null)
                .nodeTypeId(entity.getNodeTypeId())
                .costCenterTypeId(entity.getCostCenterTypeId())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .activeChildCount(0)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public CostCenterOptionResponse toOptionResponse(OrgCostCenter entity) {
        if (entity == null) return null;
        return CostCenterOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getCostCenterCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .parentId(entity.getParentCostCenter() != null ? entity.getParentCostCenter().getId() : null)
                .build();
    }

    public CostCenterUsageResponse toUsageResponse(OrgCostCenter entity, long activeChildCount) {
        if (entity == null) return null;
        boolean canDelete = activeChildCount == 0;
        boolean canDeactivate = activeChildCount == 0;
        String reason = !canDeactivate ? "ERR_ORG_0012" : null;
        return CostCenterUsageResponse.builder()
                .id(entity.getId())
                .activeChildCount(activeChildCount)
                .canDelete(canDelete)
                .canDeactivate(canDeactivate)
                .reason(reason)
                .build();
    }
}
