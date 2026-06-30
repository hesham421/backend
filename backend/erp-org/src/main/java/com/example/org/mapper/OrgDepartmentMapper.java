package com.example.org.mapper;

import com.example.org.domain.OrgBranch;
import com.example.org.domain.OrgDepartment;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgDepartmentMapper {

    public OrgDepartment toEntity(DepartmentCreateRequest request, OrgBranch branch, OrgDepartment parentDepartment) {
        if (request == null) return null;
        return OrgDepartment.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .branch(branch)
                .parentDepartment(parentDepartment)
                .nodeTypeId(request.getNodeTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgDepartment entity, DepartmentUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setNotes(request.getNotes());
        // parentDepartment FK updated by service after circular-ref check (RULE-ORG-007)
    }

    public DepartmentResponse toResponse(OrgDepartment entity) {
        if (entity == null) return null;
        return DepartmentResponse.builder()
                .id(entity.getId())
                .departmentCode(entity.getDepartmentCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .branchNameEn(entity.getBranch() != null ? entity.getBranch().getNameEn() : null)
                .parentId(entity.getParentDepartment() != null ? entity.getParentDepartment().getId() : null)
                .parentNameEn(entity.getParentDepartment() != null ? entity.getParentDepartment().getNameEn() : null)
                .nodeTypeId(entity.getNodeTypeId())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .activeChildCount(0)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public DepartmentOptionResponse toOptionResponse(OrgDepartment entity) {
        if (entity == null) return null;
        return DepartmentOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getDepartmentCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .parentId(entity.getParentDepartment() != null ? entity.getParentDepartment().getId() : null)
                .build();
    }

    public DepartmentUsageResponse toUsageResponse(OrgDepartment entity, long activeChildCount) {
        if (entity == null) return null;
        boolean canDelete = activeChildCount == 0;
        boolean canDeactivate = activeChildCount == 0;
        String reason = !canDeactivate ? "ERR_ORG_0011" : null;
        return DepartmentUsageResponse.builder()
                .id(entity.getId())
                .activeChildCount(activeChildCount)
                .canDelete(canDelete)
                .canDeactivate(canDeactivate)
                .reason(reason)
                .build();
    }
}
