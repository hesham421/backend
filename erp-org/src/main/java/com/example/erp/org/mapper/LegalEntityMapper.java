package com.example.erp.org.mapper;

import com.example.erp.org.dto.LegalEntityCreateRequest;
import com.example.erp.org.dto.LegalEntityResponse;
import com.example.erp.org.dto.LegalEntityUpdateRequest;
import com.example.erp.org.entity.OrgLegalEntity;
import org.springframework.stereotype.Component;

@Component
public class LegalEntityMapper {

    public OrgLegalEntity toEntity(LegalEntityCreateRequest request, String generatedCode) {
        if (request == null) return null;
        return OrgLegalEntity.builder()
                .legalEntityCode(generatedCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .entityTypeId(request.getEntityTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgLegalEntity entity, LegalEntityUpdateRequest request) {
        if (entity == null || request == null) return;
        // ❌ NEVER: entity.setLegalEntityCode(...) — IMMUTABLE (RULE-ORG-011/014)
        if (request.getNameAr() != null) {
            entity.setNameAr(request.getNameAr());
        }
        if (request.getNameEn() != null) {
            entity.setNameEn(request.getNameEn());
        }
        if (request.getEntityTypeId() != null) {
            entity.setEntityTypeId(request.getEntityTypeId());
        }
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
    }

    public LegalEntityResponse toResponse(OrgLegalEntity entity) {
        if (entity == null) return null;
        return LegalEntityResponse.builder()
                .id(entity.getId())
                .legalEntityCode(entity.getLegalEntityCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .entityTypeId(entity.getEntityTypeId())
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
