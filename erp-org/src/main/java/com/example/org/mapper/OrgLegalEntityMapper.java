package com.example.org.mapper;

import com.example.org.domain.OrgLegalEntity;
import com.example.org.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OrgLegalEntityMapper {

    public OrgLegalEntity toEntity(LegalEntityCreateRequest request) {
        if (request == null) return null;
        return OrgLegalEntity.builder()
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .entityTypeId(request.getEntityTypeId())
                .notes(request.getNotes())
                .isActiveFl(Boolean.TRUE)
                .build();
    }

    public void updateEntityFromRequest(OrgLegalEntity entity, LegalEntityUpdateRequest request) {
        if (entity == null || request == null) return;
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        entity.setEntityTypeId(request.getEntityTypeId());
        entity.setNotes(request.getNotes());
    }

    public LegalEntityResponse toResponse(OrgLegalEntity entity) {
        if (entity == null) return null;
        return LegalEntityResponse.builder()
                .id(entity.getId())
                .legalEntityCode(entity.getLegalEntityCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .entityTypeId(entity.getEntityTypeId())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .notes(entity.getNotes())
                .activeBranchCount(entity.getActiveBranchCount() != null ? entity.getActiveBranchCount() : 0)
                .activeProfitCenterCount(entity.getActiveProfitCenterCount() != null ? entity.getActiveProfitCenterCount() : 0)
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public LegalEntityOptionResponse toOptionResponse(OrgLegalEntity entity) {
        if (entity == null) return null;
        return LegalEntityOptionResponse.builder()
                .id(entity.getId())
                .code(entity.getLegalEntityCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .build();
    }

    public LegalEntityUsageResponse toUsageResponse(OrgLegalEntity entity, long activeBranchCount, long activeProfitCenterCount) {
        if (entity == null) return null;
        boolean canDelete = activeBranchCount == 0 && activeProfitCenterCount == 0;
        boolean canDeactivate = activeBranchCount == 0 && activeProfitCenterCount == 0;
        String reason = null;
        if (!canDeactivate) {
            if (activeBranchCount > 0) reason = "ERR_ORG_0005";
            else if (activeProfitCenterCount > 0) reason = "ERR_ORG_0006";
        }
        return LegalEntityUsageResponse.builder()
                .id(entity.getId())
                .activeBranchCount(activeBranchCount)
                .activeProfitCenterCount(activeProfitCenterCount)
                .canDelete(canDelete)
                .canDeactivate(canDeactivate)
                .reason(reason)
                .build();
    }
}
