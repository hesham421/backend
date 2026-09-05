package com.erp.mdm.mapper;

import com.erp.mdm.dto.LookupTypeCreateRequest;
import com.erp.mdm.dto.LookupTypeResponse;
import com.erp.mdm.dto.LookupTypeUpdateRequest;
import com.erp.mdm.entity.LookupType;
import org.springframework.stereotype.Component;

/** Manual entity/DTO mapper for ENTITY-MDM-001 (LookupType). */
@Component
public class LookupTypeMapper {

    public LookupType toEntity(LookupTypeCreateRequest request) {
        if (request == null) {
            return null;
        }
        return LookupType.builder()
            .typeCode(request.getTypeCode())   // NOT .toUpperCase() — LookupType.onCreate() owns it
            .nameAr(request.getNameAr())
            .nameEn(request.getNameEn())
            .notes(request.getNotes())         // isActive left to entity @Builder.Default (TRUE)
            .build();
    }

    /**
     * Mutates in place. Skips typeCode — immutable (RULE-MDM-002, structurally absent from the
     * request). No isActive handling — the update DTO carries no isActiveFl (no reactivation API).
     */
    public void updateEntityFromRequest(LookupType entity, LookupTypeUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        // notes is optional — a null (omitted) notes means "no change", so a partial update does not
        // wipe existing notes (send an empty string to clear it — matches ConfigurationMapper).
        if (request.getNotes() != null) {
            entity.setNotes(request.getNotes());
        }
    }

    public LookupTypeResponse toResponse(LookupType entity) {
        if (entity == null) {
            return null;
        }
        return LookupTypeResponse.builder()
            .id(entity.getId())
            .typeCode(entity.getTypeCode())
            .nameAr(entity.getNameAr())
            .nameEn(entity.getNameEn())
            .isActiveFl(Boolean.TRUE.equals(entity.getIsActive()))
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
