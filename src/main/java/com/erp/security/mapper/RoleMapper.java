package com.erp.security.mapper;

import com.erp.security.entity.Role;
import com.erp.security.dto.RoleDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RoleMapper {

    public static RoleDto toDto(Role entity) {
        if (entity == null) {
            return null;
        }

        return RoleDto.builder()
                .id(entity.getId())
            .roleCode(entity.getRoleCode())
                .roleName(entity.getRoleName())
            .description(entity.getDescription())
            .active(entity.getActive())
            .createdAt(entity.getCreatedAt())
            .createdBy(entity.getCreatedBy())
            .updatedAt(entity.getUpdatedAt())
            .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
