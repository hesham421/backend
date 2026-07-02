package com.example.erp.org.mapper;

import com.example.erp.org.dto.RegionCreateRequest;
import com.example.erp.org.dto.RegionResponse;
import com.example.erp.org.dto.RegionUpdateRequest;
import com.example.erp.org.entity.OrgLegalEntity;
import com.example.erp.org.entity.OrgRegion;
import com.example.erp.org.entity.OrgRegionType;
import org.springframework.stereotype.Component;

@Component
public class RegionMapper {

    public OrgRegion toEntity(RegionCreateRequest request, String generatedCode, OrgLegalEntity parent, OrgRegionType regionType) {
        if (request == null) return null;
        return OrgRegion.builder()
                .regionCode(generatedCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .legalEntity(parent)
                .regionType(regionType)
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgRegion entity, RegionUpdateRequest request) {
        if (entity == null || request == null) return;
        // ❌ NEVER: entity.setRegionCode(...) — IMMUTABLE (RULE-ORG-011/014)
        // ❌ NEVER: entity.setLegalEntity(...) / entity.setRegionType(...) — immutable, not part of update contract
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

    public RegionResponse toResponse(OrgRegion entity) {
        if (entity == null) return null;
        OrgLegalEntity parent = entity.getLegalEntity();
        OrgRegionType regionType = entity.getRegionType();
        return RegionResponse.builder()
                .id(entity.getId())
                .regionCode(entity.getRegionCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .legalEntityFk(parent != null ? parent.getId() : null)
                .legalEntityCode(parent != null ? parent.getLegalEntityCode() : null)
                .regionTypeIdFk(regionType != null ? regionType.getId() : null)
                .regionTypeNameEn(regionType != null ? regionType.getNameEn() : null)
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
