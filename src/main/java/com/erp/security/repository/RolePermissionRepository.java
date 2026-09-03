package com.erp.security.repository;

import com.erp.security.entity.RolePermission;
import com.erp.security.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
