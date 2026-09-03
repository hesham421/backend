package com.erp.security.repository;

import com.erp.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
