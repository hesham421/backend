package com.example.erp.org.mapper;

import com.example.erp.org.dto.LocationSiteCreateRequest;
import com.example.erp.org.dto.LocationSiteResponse;
import com.example.erp.org.dto.LocationSiteUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgLocationSite;
import org.springframework.stereotype.Component;

@Component
public class LocationSiteMapper {

    public OrgLocationSite toEntity(LocationSiteCreateRequest request, String generatedCode, OrgBranch parent) {
        if (request == null) return null;
        return OrgLocationSite.builder()
                .locationSiteCode(generatedCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .branch(parent)
                .siteTypeId(request.getSiteTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgLocationSite entity, LocationSiteUpdateRequest request) {
        if (entity == null || request == null) return;
        // ❌ NEVER: entity.setLocationSiteCode(...) — IMMUTABLE (RULE-ORG-011/014)
        // ❌ NEVER: entity.setBranch(...) — immutable, not part of update contract
        if (request.getNameAr() != null) {
            entity.setNameAr(request.getNameAr());
        }
        if (request.getNameEn() != null) {
            entity.setNameEn(request.getNameEn());
        }
        if (request.getSiteTypeId() != null) {
            entity.setSiteTypeId(request.getSiteTypeId());
        }
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
    }

    public LocationSiteResponse toResponse(OrgLocationSite entity) {
        if (entity == null) return null;
        OrgBranch parent = entity.getBranch();
        return LocationSiteResponse.builder()
                .id(entity.getId())
                .locationSiteCode(entity.getLocationSiteCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .branchFk(parent != null ? parent.getId() : null)
                .branchCode(parent != null ? parent.getBranchCode() : null)
                .branchNameEn(parent != null ? parent.getNameEn() : null)
                .siteTypeId(entity.getSiteTypeId())
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
