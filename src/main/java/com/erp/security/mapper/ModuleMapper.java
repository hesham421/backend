package com.erp.security.mapper;

import com.erp.security.dto.ModuleCreateRequest;
import com.erp.security.dto.ModuleResponse;
import com.erp.security.dto.ModuleUpdateRequest;
import com.erp.security.entity.Module;
import org.springframework.stereotype.Component;

/** Manual entity/DTO mapper for ENTITY-SEC-010 (Module). */
@Component
public class ModuleMapper {

    public Module toEntity(ModuleCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Module.builder()
            .moduleCode(request.getModuleCode())   // NOT .toUpperCase() — Module.onCreate() owns it
            .nameAr(request.getNameAr())
            .nameEn(request.getNameEn())
            .isActive(request.getIsActiveFl() != null ? request.getIsActiveFl() : Boolean.TRUE)
            .build();
    }

    /**
     * Mutates in place. Skips moduleCode — immutable (RULE-SEC-010, structurally absent from the
     * request). isActiveFl, when present, is applied via activate()/deactivate() (never a raw
     * setter); null means "no change".
     */
    public void updateEntityFromRequest(Module entity, ModuleUpdateRequest request) {
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

    public ModuleResponse toResponse(Module entity) {
        if (entity == null) {
            return null;
        }
        return ModuleResponse.builder()
            .id(entity.getId())
            .moduleCode(entity.getModuleCode())
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
