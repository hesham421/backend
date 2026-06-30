package com.example.org.mapper;

import com.example.org.domain.OrgBranch;
import com.example.org.domain.OrgLocationSite;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgLocationSiteMapper {

    public OrgLocationSite toEntity(LocationSiteCreateRequest request, OrgBranch branch) {
        if (request == null) return null;
        return OrgLocationSite.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .branch(branch)
                .siteTypeId(request.getSiteTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgLocationSite entity, LocationSiteUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setSiteTypeId(request.getSiteTypeId());
        entity.setNotes(request.getNotes());
    }

    public LocationSiteResponse toResponse(OrgLocationSite entity) {
        if (entity == null) return null;
        return LocationSiteResponse.builder()
                .id(entity.getId())
                .locationSiteCode(entity.getLocationSiteCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .branchNameEn(entity.getBranch() != null ? entity.getBranch().getNameEn() : null)
                .siteTypeId(entity.getSiteTypeId())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public LocationSiteOptionResponse toOptionResponse(OrgLocationSite entity) {
        if (entity == null) return null;
        return LocationSiteOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getLocationSiteCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchId(entity.getBranch() != null ? entity.getBranch().getId() : null)
                .build();
    }

    public LocationSiteUsageResponse toUsageResponse(OrgLocationSite entity) {
        if (entity == null) return null;
        return LocationSiteUsageResponse.builder()
                .id(entity.getId())
                .canDelete(true)
                .canDeactivate(true)
                .reason(null)
                .build();
    }
}
