package com.erp.security.mapper;

import com.erp.security.entity.Permission;
import com.erp.security.dto.PermissionDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PermissionMapper {

    public static PermissionDto toDto(Permission entity) {
        if (entity == null) {
            return null;
        }

        PermissionDto.PermissionDtoBuilder builder = PermissionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(null) // Permission entity does not have description field
                .permissionType(entity.getPermissionType() != null ? entity.getPermissionType().name() : null);

        if (entity.getPage() != null) {
            builder.pageId(entity.getPage().getId());
            builder.pageCode(entity.getPage().getPageCode());
        }

        return builder.build();
    }
}
