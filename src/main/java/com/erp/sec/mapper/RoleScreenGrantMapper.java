package com.erp.sec.mapper;

import com.erp.sec.dto.RoleScreenGrantResponse;
import com.erp.sec.entity.Role;
import com.erp.sec.entity.RoleScreenGrant;
import com.erp.sec.entity.ScreenRegistry;
import org.springframework.stereotype.Component;

/**
 * Manual entity/DTO mapper for ENT-SEC-008 (RoleScreenGrant). Both parents are parameters so the
 * caller cannot leave an FK unset; {@code grantedBy} is system-set by the service.
 */
@Component
public class RoleScreenGrantMapper {

    public RoleScreenGrant toEntity(Role role, ScreenRegistry screen) {
        if (role == null || screen == null) {
            return null;
        }
        return RoleScreenGrant.builder()
            .role(role)
            .screen(screen)
            .build();
    }

    public RoleScreenGrantResponse toResponse(RoleScreenGrant entity) {
        if (entity == null) {
            return null;
        }
        return RoleScreenGrantResponse.builder()
            .roleScreenGrantPk(entity.getRoleScreenGrantPk())
            .roleId(entity.getRole() == null ? null : entity.getRole().getRolePk())
            .screenId(entity.getScreen() == null ? null : entity.getScreen().getScreenRegPk())
            .grantedBy(entity.getGrantedBy())
            .grantedAt(entity.getGrantedAt())
            .build();
    }
}
