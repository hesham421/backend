package com.erp.security.repository;

import com.erp.security.entity.Page;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-004 (Page, screen registry / CORE-9 owner). pageCode is the immutable
 * natural key (RULE-SEC-010), so no {@code existsBy...AndIdNot} variant is provided — it can never
 * change on update. Pages are addressed by surrogate id (API-SEC-013 {@code /pages/{id}}), so
 * uniqueness is served by {@code existsByPageCode} alone. The moduleFk EXACT filter (QR-SEC-0012)
 * is a nested association path, so it is applied by an explicit Specification in PageService rather
 * than a derived method here.
 */
@Repository
public interface PageRepository
    extends JpaRepository<Page, Long>,
            JpaSpecificationExecutor<Page> {

    /** QR-SEC-0014 (RULE-SEC-010) — pageCode uniqueness pre-check for API-SEC-013 create. */
    boolean existsByPageCode(String pageCode);

    /**
     * QR-SEC-0032 (API-SEC-022 menu) — the ACTIVE Pages belonging to any of the caller's granted
     * ACTIVE Modules (RULE-SEC-017's module-scope half). SEC_USER_ACCOUNT → SEC_USER_ROLE →
     * SEC_ROLE → SEC_ROLE_MODULE → SEC_MODULE → SEC_PAGE.MODULE_FK. The isActive guards on
     * Role/Module/Page mirror QR-SEC-0028 so a deactivated role/module/page never surfaces in the
     * tree. The VIEW-or-orphan-branch accessibility decision (RULE-SEC-017/018) and the
     * parentPageFk nesting are business logic computed by MenuService over this row set — a
     * multi-row tree-shaping concern, not a single query.
     */
    @Query("""
        SELECT DISTINCT p
        FROM Page p, Module m, RoleModule rm, Role r, UserRole ur, UserAccount u
        WHERE u.username = :username
          AND ur.id.userAccountFk = u.id
          AND r.id = ur.id.roleFk
          AND r.isActive = true
          AND rm.id.roleFk = ur.id.roleFk
          AND m.id = rm.id.moduleFk
          AND m.isActive = true
          AND p.module.id = m.id
          AND p.isActive = true
        ORDER BY p.nameEn
        """)
    List<Page> findActivePagesForGrantedModulesByUsername(@Param("username") String username);
}
