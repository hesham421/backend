package com.example.org.mapper;

import com.example.org.domain.OrgLegalEntity;
import com.example.org.domain.OrgProfitCenter;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgProfitCenterMapper {

    public OrgProfitCenter toEntity(ProfitCenterCreateRequest request, OrgLegalEntity legalEntity) {
        if (request == null) return null;
        return OrgProfitCenter.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .legalEntity(legalEntity)
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgProfitCenter entity, ProfitCenterUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setNotes(request.getNotes());
    }

    public ProfitCenterResponse toResponse(OrgProfitCenter entity) {
        if (entity == null) return null;
        return ProfitCenterResponse.builder()
                .id(entity.getId())
                .profitCenterCode(entity.getProfitCenterCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityId(entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null)
                .legalEntityNameEn(entity.getLegalEntity() != null ? entity.getLegalEntity().getNameEn() : null)
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public ProfitCenterOptionResponse toOptionResponse(OrgProfitCenter entity) {
        if (entity == null) return null;
        return ProfitCenterOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getProfitCenterCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityId(entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null)
                .build();
    }

    public ProfitCenterUsageResponse toUsageResponse(OrgProfitCenter entity) {
        if (entity == null) return null;
        return ProfitCenterUsageResponse.builder()
                .id(entity.getId())
                .canDelete(true)
                .canDeactivate(true)
                .reason(null)
                .build();
    }
}
