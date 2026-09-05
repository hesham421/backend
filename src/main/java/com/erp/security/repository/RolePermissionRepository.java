package com.erp.security.repository;

import com.erp.security.entity.Permission;
import com.erp.security.entity.RolePermission;
import com.erp.security.entity.RolePermissionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-009 (RolePermission, Tier-2 grant join). The idempotent grant/revoke
 * (API-SEC-015, QR-SEC-0018) is served entirely by the inherited composite-key methods
 * ({@code existsById}/{@code save}/{@code deleteById} on {@link RolePermissionId}). The
 * revoke-block dependents query (QR-SEC-0029) is owned by RoleModuleRepository — not duplicated here.
 */
@Repository
public interface RolePermissionRepository
    extends JpaRepository<RolePermission, RolePermissionId>,
            JpaSpecificationExecutor<RolePermission> {

    /**
     * The Tier-2 screen permissions currently granted to a role — the read counterpart of the
     * grant/revoke join (API-SEC-015), used to pre-populate the role screen's permission picker in
     * edit mode. Returns every current grant (active or not) so the picker reflects true state;
     * ordered by permission id for deterministic output. Empty when the role holds no permission.
     */
    @Query("""
        SELECT p
        FROM RolePermission rp, Permission p
        WHERE rp.id.roleFk = :roleId
          AND p.id = rp.id.permissionFk
        ORDER BY p.id
        """)
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);
}
