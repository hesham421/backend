package com.erp.security.repository;

import com.erp.security.entity.Module;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-010 (Module). moduleCode is the immutable natural key (RULE-SEC-010),
 * so no {@code existsBy...AndIdNot} variant is provided — it can never change on update. Modules
 * are addressed by surrogate id (API-SEC-020 {@code /modules/{id}}), so uniqueness is served by
 * {@code existsByModuleCode} alone.
 */
@Repository
public interface ModuleRepository
    extends JpaRepository<Module, Long>,
            JpaSpecificationExecutor<Module> {

    boolean existsByModuleCode(String moduleCode);

    /**
     * QR-SEC-0028 (API-SEC-019 dashboard) — the distinct ACTIVE modules granted to a user through
     * any of its ACTIVE roles. SEC_USER_ACCOUNT → SEC_USER_ROLE → SEC_ROLE → SEC_ROLE_MODULE →
     * SEC_MODULE. The role.isActive guard keeps a deactivated role (whose SEC_ROLE_MODULE join rows
     * survive deactivation) from continuing to surface its modules on the dashboard (RULE-SEC-013).
     */
    @Query("""
        SELECT DISTINCT m
        FROM Module m, RoleModule rm, Role r, UserRole ur, UserAccount u
        WHERE u.username = :username
          AND ur.id.userAccountFk = u.id
          AND r.id = ur.id.roleFk
          AND r.isActive = true
          AND rm.id.roleFk = ur.id.roleFk
          AND m.id = rm.id.moduleFk
          AND m.isActive = true
        ORDER BY m.moduleCode
        """)
    List<Module> findGrantedActiveModulesByUsername(@Param("username") String username);
}
