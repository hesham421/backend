package com.erp.security.mapper;

import com.erp.security.dto.RoleCreateRequest;
import com.erp.security.dto.RoleResponse;
import com.erp.security.dto.RoleUpdateRequest;
import com.erp.security.entity.Role;
import org.springframework.stereotype.Component;

/** Manual entity/DTO mapper for ENTITY-SEC-002 (Role). */
@Component
public class RoleMapper {

    public Role toEntity(RoleCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Role.builder()
            .roleCode(request.getRoleCode())   // NOT .toUpperCase() — Role.onCreate() owns it
            .nameAr(request.getNameAr())
            .nameEn(request.getNameEn())
            .isActive(request.getIsActiveFl() != null ? request.getIsActiveFl() : Boolean.TRUE)
            .build();
    }

    /**
     * Mutates in place. Skips roleCode — immutable (RULE-SEC-010, structurally absent from the
     * request). isActiveFl, when present, is applied via activate()/deactivate() (never a raw
     * setter); null means "no change".
     */
    public void updateEntityFromRequest(Role entity, RoleUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setNameAr(request.getNameAr());
        entity.setNameEn(request.getNameEn());
        if (request.getIsActiveFl() != null) {
            if (Boolean.TRUE.equals(request.getIsActiveFl())) {
                entity.activate();
            } else {
                entity.deactivate();
            }
        }
    }

    public RoleResponse toResponse(Role entity) {
        if (entity == null) {
            return null;
        }
        return RoleResponse.builder()
            .id(entity.getId())
            .roleCode(entity.getRoleCode())
            .nameAr(entity.getNameAr())
            .nameEn(entity.getNameEn())
            .isActiveFl(Boolean.TRUE.equals(entity.getIsActive()))
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
            .build();
    }
}
