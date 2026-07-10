package com.example.security.mapper;

import com.example.security.dto.SecRoleBranchDto;
import com.example.security.entity.SecRoleBranch;
import lombok.experimental.UtilityClass;

/**
 * Mapper for SecRoleBranch entity to SecRoleBranchDto.
 */
@UtilityClass
public class SecRoleBranchMapper {

    public static SecRoleBranchDto toDto(SecRoleBranch entity) {
        if (entity == null) {
            return null;
        }
        return SecRoleBranchDto.builder()
                .roleIdFk(entity.getRoleIdFk())
                .branchIdFk(entity.getBranchIdFk())
                .dataAccessLevel(entity.getDataAccessLevel())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
