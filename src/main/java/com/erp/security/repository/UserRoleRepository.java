package com.erp.security.repository;

import com.erp.security.entity.UserRole;
import com.erp.security.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-008 (UserRole, role-assignment join). The assignment (API-SEC-012) is
 * served by the inherited composite-key methods ({@code existsById}/{@code save} on
 * {@link UserRoleId}) — QR-SEC-0017 — for an idempotent insert into SEC_USER_ROLE.
 */
@Repository
public interface UserRoleRepository
    extends JpaRepository<UserRole, UserRoleId>,
            JpaSpecificationExecutor<UserRole> {
}
