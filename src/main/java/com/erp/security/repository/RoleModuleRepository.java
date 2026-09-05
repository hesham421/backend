package com.erp.security.repository;

import com.erp.security.entity.Module;
import com.erp.security.entity.RoleModule;
import com.erp.security.entity.RoleModuleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-011 (RoleModule, Tier-1 grant join). The grant itself is served by the
 * inherited composite-key methods ({@code existsById}/{@code save}/{@code deleteById} on
 * {@link RoleModuleId}) — QR-SEC-0026. The read-only dependents check (QR-SEC-0029) is co-located
 * here rather than on a Permission/RolePermission repository: SVC-API-RBAC OWNS those CRUD repos,
 * so this avoids a second, competing file. RBAC must not duplicate this single query.
 */
@Repository
public interface RoleModuleRepository
    extends JpaRepository<RoleModule, RoleModuleId>,
            JpaSpecificationExecutor<RoleModule> {

    /**
     * QR-SEC-0029 (API-SEC-018 revoke-block, RULE-SEC-014) — true if the role still holds ANY
     * screen permission for a page belonging to the given module. SEC_ROLE_PERMISSION →
     * SEC_PERMISSION → SEC_PAGE.MODULE_FK. Feeds
     * AuthorizationGrantDomainService.assertModuleRevokeAllowed(...).
     */
    @Query("""
        SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END
        FROM RolePermission rp, Permission p
        WHERE rp.id.roleFk = :roleId
          AND p.id = rp.id.permissionFk
          AND p.page.module.id = :moduleId
        """)
    boolean existsRolePermissionForRoleInModule(@Param("roleId") Long roleId,
                                                 @Param("moduleId") Long moduleId);

    /**
     * The Tier-1 modules currently granted to a role — the read counterpart of the
     * assign/revoke join (API-SEC-017/018), used to pre-populate the role screen's module picker in
     * edit mode. Returns every current grant (active or not) so the picker reflects true state;
     * ordered by module id for deterministic output. Empty when the role holds no module.
     */
    @Query("""
        SELECT m
        FROM RoleModule rm, Module m
        WHERE rm.id.roleFk = :roleId
          AND m.id = rm.id.moduleFk
        ORDER BY m.id
        """)
    List<Module> findModulesByRoleId(@Param("roleId") Long roleId);
}
