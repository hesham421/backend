package com.erp.masterdata.mapper;

import com.erp.masterdata.dto.*;
import com.erp.masterdata.entity.MdLookupDetail;
import com.erp.masterdata.entity.MdMasterLookup;
import org.springframework.stereotype.Component;

@Component
public class LookupDetailMapper {

    /**
     * Parent entity is a required parameter — enforces the FK at compile time rather than relying
     * on the caller to remember calling {@code setMasterLookup()} afterwards.
     */
    public MdLookupDetail toEntity(LookupDetailCreateRequest request, MdMasterLookup masterLookup) {
        if (request == null) {
            return null;
        }

        return MdLookupDetail.builder()
                .code(request.getCode())
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .extraValue(request.getExtraValue())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
            .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .masterLookup(masterLookup)
                .build();
    }

    /**
     * masterLookupId and code are immutable and must not be updated here.
     */
    public void updateEntityFromRequest(MdLookupDetail entity, LookupDetailUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }

        // Note: code is NOT updated - it is immutable per contract
        // Note: masterLookupId is NOT updated - it is immutable per contract
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setExtraValue(request.getExtraValue());
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
    }

    public LookupDetailResponse toResponse(MdLookupDetail entity) {
        if (entity == null) {
            return null;
        }

        MdMasterLookup masterLookup = entity.getMasterLookup();

        return LookupDetailResponse.builder()
                .id(entity.getId())
                .masterLookupId(masterLookup != null ? masterLookup.getId() : null)
                .masterLookupKey(masterLookup != null ? masterLookup.getLookupKey() : null)
                .masterLookupName(masterLookup != null ? masterLookup.getLookupName() : null)
                .code(entity.getCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .extraValue(entity.getExtraValue())
                .sortOrder(entity.getSortOrder())
                .isActive(Boolean.TRUE.equals(entity.getIsActive()))
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public LookupDetailOptionResponse toOptionResponse(MdLookupDetail entity) {
        if (entity == null) {
            return null;
        }

        return LookupDetailOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .extraValue(entity.getExtraValue())
                .sortOrder(entity.getSortOrder())
                .build();
    }

    public LookupDetailUsageResponse toUsageResponse(MdLookupDetail entity) {
        
        if (entity == null) {
            return null;
        }

        return LookupDetailUsageResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .totalReferencesCount(0L)
                .canBeDeleted(true)
                .reason(null)
                .build();
    }
}
