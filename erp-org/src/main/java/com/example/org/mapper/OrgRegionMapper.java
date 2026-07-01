package com.example.org.mapper;

import com.example.org.domain.OrgLegalEntity;
import com.example.org.domain.OrgRegion;
import com.example.org.domain.OrgRegionType;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgRegionMapper {

    public OrgRegion toEntity(RegionCreateRequest request, OrgLegalEntity legalEntity, OrgRegionType regionType) {
        if (request == null) return null;
        return OrgRegion.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .legalEntity(legalEntity)
                .regionType(regionType)
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgRegion entity, RegionUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setNotes(request.getNotes());
        // regionType FK updated by service after loading the referenced entity
    }

    public RegionResponse toResponse(OrgRegion entity) {
        if (entity == null) return null;
        return RegionResponse.builder()
                .id(entity.getId())
                .regionCode(entity.getRegionCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityId(entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null)
                .legalEntityNameEn(entity.getLegalEntity() != null ? entity.getLegalEntity().getNameEn() : null)
                .regionTypeId(entity.getRegionType() != null ? entity.getRegionType().getId() : null)
                .regionTypeNameEn(entity.getRegionType() != null ? entity.getRegionType().getNameEn() : null)
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public RegionOptionResponse toOptionResponse(OrgRegion entity) {
        if (entity == null) return null;
        return RegionOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getRegionCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityId(entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null)
                .build();
    }

    public RegionUsageResponse toUsageResponse(OrgRegion entity) {
        if (entity == null) return null;
        // OQ-001 DEFERRED — RULE-ORG-006/017 region deactivation guard pending resolution
        return RegionUsageResponse.builder()
                .id(entity.getId())
                .canDelete(true)
                .canDeactivate(true)
                .reason(null)
                .build();
    }
}
