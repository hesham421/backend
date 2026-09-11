package com.erp.sec.mapper;

import com.erp.sec.dto.ModuleGrantRevokeResponse;
import com.erp.sec.dto.RoleModuleGrantResponse;
import com.erp.sec.entity.ModuleRegistry;
import com.erp.sec.entity.Role;
import com.erp.sec.entity.RoleModuleGrant;
import org.springframework.stereotype.Component;

/**
 * Manual entity/DTO mapper for ENT-SEC-007 (RoleModuleGrant). Both parents are parameters so the
 * caller cannot leave an FK unset; {@code grantedBy} is system-set by the service.
 */
@Component
public class RoleModuleGrantMapper {

    public RoleModuleGrant toEntity(Role role, ModuleRegistry module) {
        if (role == null || module == null) {
            return null;
        }
        return RoleModuleGrant.builder()
            .role(role)
            .module(module)
            .build();
    }

    public RoleModuleGrantResponse toResponse(RoleModuleGrant entity) {
        if (entity == null) {
            return null;
        }
        return RoleModuleGrantResponse.builder()
            .roleModuleGrantPk(entity.getRoleModuleGrantPk())
            .roleId(entity.getRole() == null ? null : entity.getRole().getRolePk())
            .moduleId(entity.getModule() == null ? null : entity.getModule().getModuleRegPk())
            .grantedBy(entity.getGrantedBy())
            .grantedAt(entity.getGrantedAt())
            .build();
    }

    /** API-SEC-015 confirmation — the counts come from the cascade the service actually performed. */
    public ModuleGrantRevokeResponse toRevokeResponse(int revokedScreenGrants, int revokedActionGrants) {
        return ModuleGrantRevokeResponse.builder()
            .revokedScreenGrants(revokedScreenGrants)
            .revokedActionGrants(revokedActionGrants)
            .build();
    }
}
