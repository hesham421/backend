package com.example.erp.org.mapper;

import com.example.erp.org.dto.BranchCreateRequest;
import com.example.erp.org.dto.BranchResponse;
import com.example.erp.org.dto.BranchUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgLegalEntity;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public OrgBranch toEntity(BranchCreateRequest request, String generatedCode, OrgLegalEntity parent) {
        if (request == null) return null;
        return OrgBranch.builder()
                .branchCode(generatedCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .legalEntity(parent)
                .branchTypeId(request.getBranchTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgBranch entity, BranchUpdateRequest request) {
        if (entity == null || request == null) return;
        // ❌ NEVER: entity.setBranchCode(...) — IMMUTABLE (RULE-ORG-011/014)
        // ❌ NEVER: entity.setLegalEntity(...) — parent FK is immutable, not part of update contract
        if (request.getNameAr() != null) {
            entity.setNameAr(request.getNameAr());
        }
        if (request.getNameEn() != null) {
            entity.setNameEn(request.getNameEn());
        }
        if (request.getBranchTypeId() != null) {
            entity.setBranchTypeId(request.getBranchTypeId());
        }
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
    }

    public BranchResponse toResponse(OrgBranch entity) {
        if (entity == null) return null;
        OrgLegalEntity parent = entity.getLegalEntity();
        return BranchResponse.builder()
                .id(entity.getId())
                .branchCode(entity.getBranchCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityFk(parent != null ? parent.getId() : null)
                .legalEntityCode(parent != null ? parent.getLegalEntityCode() : null)
                .legalEntityNameEn(parent != null ? parent.getNameEn() : null)
                .branchTypeId(entity.getBranchTypeId())
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
