package com.example.org.mapper;

import com.example.org.domain.OrgRegionType;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgRegionTypeMapper {

    public OrgRegionType toEntity(RegionTypeCreateRequest request) {
        if (request == null) return null;
        return OrgRegionType.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgRegionType entity, RegionTypeUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
    }

    public RegionTypeResponse toResponse(OrgRegionType entity) {
        if (entity == null) return null;
        return RegionTypeResponse.builder()
                .id(entity.getId())
                .regionTypeCode(entity.getRegionTypeCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public RegionTypeOptionResponse toOptionResponse(OrgRegionType entity) {
        if (entity == null) return null;
        return RegionTypeOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getRegionTypeCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .build();
    }

    public RegionTypeUsageResponse toUsageResponse(OrgRegionType entity, long activeRegionCount) {
        if (entity == null) return null;
        boolean canDelete = activeRegionCount == 0;
        boolean canDeactivate = activeRegionCount == 0;
        String reason = !canDeactivate ? "ERR_ORG_0010" : null;
        return RegionTypeUsageResponse.builder()
                .id(entity.getId())
                .activeRegionCount(activeRegionCount)
                .canDelete(canDelete)
                .canDeactivate(canDeactivate)
                .reason(reason)
                .build();
    }
}
