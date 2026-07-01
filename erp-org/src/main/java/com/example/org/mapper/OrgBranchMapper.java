package com.example.org.mapper;

import com.example.org.domain.OrgBranch;
import com.example.org.domain.OrgLegalEntity;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgBranchMapper {

    public OrgBranch toEntity(BranchCreateRequest request, OrgLegalEntity legalEntity) {
        if (request == null) return null;
        return OrgBranch.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .legalEntity(legalEntity)
                .branchTypeId(request.getBranchTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgBranch entity, BranchUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setBranchTypeId(request.getBranchTypeId());
        entity.setNotes(request.getNotes());
    }

    public BranchResponse toResponse(OrgBranch entity) {
        if (entity == null) return null;
        return BranchResponse.builder()
                .id(entity.getId())
                .branchCode(entity.getBranchCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityId(entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null)
                .legalEntityNameEn(entity.getLegalEntity() != null ? entity.getLegalEntity().getNameEn() : null)
                .branchTypeId(entity.getBranchTypeId())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .activeDepartmentCount(entity.getActiveDepartmentCount() != null ? entity.getActiveDepartmentCount() : 0)
                .activeCostCenterCount(entity.getActiveCostCenterCount() != null ? entity.getActiveCostCenterCount() : 0)
                .activeLocationSiteCount(entity.getActiveLocationSiteCount() != null ? entity.getActiveLocationSiteCount() : 0)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public BranchOptionResponse toOptionResponse(OrgBranch entity) {
        if (entity == null) return null;
        return BranchOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getBranchCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityId(entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null)
                .build();
    }

    public BranchUsageResponse toUsageResponse(OrgBranch entity, long activeDeptCount, long activeCcCount, long activeLsCount) {
        if (entity == null) return null;
        boolean canDelete = activeDeptCount == 0 && activeCcCount == 0 && activeLsCount == 0;
        boolean canDeactivate = activeDeptCount == 0 && activeCcCount == 0 && activeLsCount == 0;
        String reason = null;
        if (!canDeactivate) {
            if (activeDeptCount > 0) reason = "ERR_ORG_0007";
            else if (activeCcCount > 0) reason = "ERR_ORG_0008";
            else if (activeLsCount > 0) reason = "ERR_ORG_0009";
        }
        return BranchUsageResponse.builder()
                .id(entity.getId())
                .activeDepartmentCount(activeDeptCount)
                .activeCostCenterCount(activeCcCount)
                .activeLocationSiteCount(activeLsCount)
                .canDelete(canDelete)
                .canDeactivate(canDeactivate)
                .reason(reason)
                .build();
    }
}
