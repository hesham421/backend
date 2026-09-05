package com.erp.mdm.mapper;

import com.erp.mdm.dto.LookupValueCreateRequest;
import com.erp.mdm.dto.LookupValueResponse;
import com.erp.mdm.dto.LookupValueUpdateRequest;
import com.erp.mdm.entity.LookupValue;
import org.springframework.stereotype.Component;

/**
 * Manual entity/DTO mapper for ENTITY-MDM-002 (LookupValue). Scalar-only, mirroring the sole existing
 * child mapper {@code PageMapper}: the {@code lookupType} association is resolved and set by
 * MdmLookupValueService (mappers stay query-free — build-create-mapper SH.3), so toEntity /
 * updateEntityFromRequest never touch the FK relationship. This is a deliberate deviation from
 * build-create-mapper A.4.2 ("child toEntity accepts the parent entity as a parameter"), matching
 * PageMapper + SH.3. toResponse reads the parent id off the (lazy) association.
 */
@Component
public class LookupValueMapper {

    public LookupValue toEntity(LookupValueCreateRequest request) {
        if (request == null) {
            return null;
        }
        return LookupValue.builder()
            .valueCode(request.getValueCode())   // NOT .toUpperCase() — LookupValue.onCreate() owns it
            .nameAr(request.getNameAr())
            .nameEn(request.getNameEn())
            .sortOrder(request.getSortOrder())   // isActive left to entity @Builder.Default (TRUE)
            .build();
    }

    /**
     * Mutates scalar fields in place. Skips valueCode — immutable (RULE-MDM-004, structurally absent
     * from the request) — and the lookupType association (immutable parent). No isActive handling —
     * the update DTO carries no isActiveFl (no reactivation API).
     */
    public void updateEntityFromRequest(LookupValue entity, LookupValueUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        // sortOrder is optional — a null (omitted) sortOrder means "no change", so a partial update
        // does not wipe the existing display order (matches ConfigurationMapper's notes convention).
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
    }

    public LookupValueResponse toResponse(LookupValue entity) {
        if (entity == null) {
            return null;
        }
        return LookupValueResponse.builder()
            .id(entity.getId())
            .lookupTypeFk(entity.getLookupType() != null ? entity.getLookupType().getId() : null)
            .valueCode(entity.getValueCode())
            .nameAr(entity.getNameAr())
            .nameEn(entity.getNameEn())
            .sortOrder(entity.getSortOrder())
            .isActiveFl(Boolean.TRUE.equals(entity.getIsActive()))
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
