package com.example.erp.org.mapper;

import com.example.erp.org.dto.DepartmentCreateRequest;
import com.example.erp.org.dto.DepartmentResponse;
import com.example.erp.org.dto.DepartmentUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgDepartment;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public OrgDepartment toEntity(DepartmentCreateRequest request, String generatedCode, OrgBranch parent, OrgDepartment parentDepartment) {
        if (request == null) return null;
        return OrgDepartment.builder()
                .departmentCode(generatedCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .branch(parent)
                .parentDepartment(parentDepartment)
                .nodeTypeId(request.getNodeTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    /** Parent reassignment (cycle-checked by the Service/Domain) is applied separately via {@code entity.setParentDepartment(...)}. */
    public void updateEntityFromRequest(OrgDepartment entity, DepartmentUpdateRequest request) {
        if (entity == null || request == null) return;
        // ❌ NEVER: entity.setDepartmentCode(...) — IMMUTABLE (RULE-ORG-011/014)
        // ❌ NEVER: entity.setBranch(...) — immutable, not part of update contract
        // ❌ NEVER: entity.setNodeTypeId(...) — IMMUTABLE (RULE-ORG-020)
        if (request.getNameAr() != null) {
            entity.setNameAr(request.getNameAr());
        }
        if (request.getNameEn() != null) {
            entity.setNameEn(request.getNameEn());
        }
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
    }

    public DepartmentResponse toResponse(OrgDepartment entity) {
        if (entity == null) return null;
        OrgBranch branch = entity.getBranch();
        OrgDepartment parent = entity.getParentDepartment();
        return DepartmentResponse.builder()
                .id(entity.getId())
                .departmentCode(entity.getDepartmentCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchFk(branch != null ? branch.getId() : null)
                .parentDepartmentFk(parent != null ? parent.getId() : null)
                .nodeTypeId(entity.getNodeTypeId())
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
