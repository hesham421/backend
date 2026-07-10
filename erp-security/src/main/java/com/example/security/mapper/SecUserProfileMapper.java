package com.example.security.mapper;

import com.example.security.dto.SecUserProfileDto;
import com.example.security.entity.SecUserProfile;
import lombok.experimental.UtilityClass;

/**
 * Mapper for SecUserProfile entity to SecUserProfileDto.
 */
@UtilityClass
public class SecUserProfileMapper {

    public static SecUserProfileDto toDto(SecUserProfile entity) {
        if (entity == null) {
            return null;
        }
        return SecUserProfileDto.builder()
                .userIdFk(entity.getUserIdFk())
                .branchIdFk(entity.getBranchIdFk())
                .fullNameAr(entity.getFullNameAr())
                .fullNameEn(entity.getFullNameEn())
                .preferredLang(entity.getPreferredLang())
                .employeeIdFk(entity.getEmployeeIdFk())
                .isActiveFl(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
