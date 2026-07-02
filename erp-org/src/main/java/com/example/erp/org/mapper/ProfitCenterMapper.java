package com.example.erp.org.mapper;

import com.example.erp.org.dto.ProfitCenterCreateRequest;
import com.example.erp.org.dto.ProfitCenterResponse;
import com.example.erp.org.dto.ProfitCenterUpdateRequest;
import com.example.erp.org.entity.OrgLegalEntity;
import com.example.erp.org.entity.OrgProfitCenter;
import org.springframework.stereotype.Component;

@Component
public class ProfitCenterMapper {

    public OrgProfitCenter toEntity(ProfitCenterCreateRequest request, String generatedCode, OrgLegalEntity parent) {
        if (request == null) return null;
        return OrgProfitCenter.builder()
                .profitCenterCode(generatedCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .legalEntity(parent)
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgProfitCenter entity, ProfitCenterUpdateRequest request) {
        if (entity == null || request == null) return;
        // ❌ NEVER: entity.setProfitCenterCode(...) — IMMUTABLE (RULE-ORG-011/014)
        // ❌ NEVER: entity.setLegalEntity(...) — immutable, not part of update contract
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

    public ProfitCenterResponse toResponse(OrgProfitCenter entity) {
        if (entity == null) return null;
        OrgLegalEntity parent = entity.getLegalEntity();
        return ProfitCenterResponse.builder()
                .id(entity.getId())
                .profitCenterCode(entity.getProfitCenterCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityFk(parent != null ? parent.getId() : null)
                .legalEntityCode(parent != null ? parent.getLegalEntityCode() : null)
                .legalEntityNameEn(parent != null ? parent.getNameEn() : null)
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
