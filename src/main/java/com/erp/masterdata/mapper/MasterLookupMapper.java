package com.erp.masterdata.mapper;

import com.erp.masterdata.dto.*;
import com.erp.masterdata.entity.MdMasterLookup;
import org.springframework.stereotype.Component;

@Component
public class MasterLookupMapper {

    public MdMasterLookup toEntity(MasterLookupCreateRequest request) {
        if (request == null) {
            return null;
        }

        return MdMasterLookup.builder()
                .lookupKey(request.getLookupKey())
                .lookupName(request.getLookupName())
                .lookupNameEn(request.getLookupNameEn())
                .description(request.getDescription())
            .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .build();
    }

    /**
     * lookupKey is immutable and must not be updated here.
     */
    public void updateEntityFromRequest(MdMasterLookup entity, MasterLookupUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }

        // Note: lookupKey is NOT updated - it is immutable per contract
        entity.setLookupName(request.getLookupName());
        entity.setLookupNameEn(request.getLookupNameEn());
        entity.setDescription(request.getDescription());
    }

    public MasterLookupResponse toResponse(MdMasterLookup entity) {
        if (entity == null) {
            return null;
        }

        return MasterLookupResponse.builder()
                .id(entity.getId())
                .lookupKey(entity.getLookupKey())
                .lookupName(entity.getLookupName())
                .lookupNameEn(entity.getLookupNameEn())
                .description(entity.getDescription())
            .isActive(Boolean.TRUE.equals(entity.getIsActive()))
                .detailCount(entity.getDetailCount())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public MasterLookupUsageResponse toUsageResponse(
            MdMasterLookup entity,
            long totalDetailsCount,
            long activeDetailsCount) {
        
        if (entity == null) {
            return null;
        }

        boolean canDelete = totalDetailsCount == 0;
        boolean canDeactivate = activeDetailsCount == 0;

        return MasterLookupUsageResponse.builder()
                .masterLookupId(entity.getId())
                .lookupKey(entity.getLookupKey())
                .totalDetails(totalDetailsCount)
                .activeDetails(activeDetailsCount)
                .canDelete(canDelete)
                .canDeactivate(canDeactivate)
                .build();
    }
}
