package com.erp.security.repository;

import com.erp.security.entity.Role;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-002 (Role). Created minimally by the SVC-API-MODULES sub, which needs
 * only the inherited {@code findById}/{@code existsById} to validate a role before a Tier-1
 * module grant (API-SEC-017/018). The SVC-API-RBAC sub OWNS Role CRUD and EXTENDS this same
 * interface additively (roleCode uniqueness checks, finders) — build clean, do not overfit.
 */
@Repository
public interface RoleRepository
    extends JpaRepository<Role, Long>,
            JpaSpecificationExecutor<Role> {

    /**
     * QR-SEC-0010 (RULE-SEC-010) — roleCode uniqueness pre-check for API-SEC-011 create. roleCode
     * is the immutable natural key (never changes on update), so no {@code existsBy...AndIdNot}
     * variant is provided.
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * QR-SEC-0030 (API-SEC-021 self) — the caller's currently ACTIVE roles, for the
     * roleCodes[]/roleNames[] union (RULE-SEC-016). SEC_USER_ACCOUNT → SEC_USER_ROLE → SEC_ROLE.
     * The role.isActive guard mirrors QR-SEC-0028 so a deactivated role is not reported as
     * currently held.
     */
    @Query("""
        SELECT DISTINCT r
        FROM Role r, UserRole ur, UserAccount u
        WHERE u.username = :username
          AND ur.id.userAccountFk = u.id
          AND r.id = ur.id.roleFk
          AND r.isActive = true
        ORDER BY r.roleCode
        """)
    List<Role> findActiveRolesByUsername(@Param("username") String username);
}
