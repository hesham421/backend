package com.erp.security.repository;

import com.erp.security.entity.Permission;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-003 (Permission). Serves two callers: API-SEC-013 page-create
 * generation (RULE-SEC-011) needs the codes already registered for a page as the collision
 * pre-check ({@link #findPermissionCodesByPageId}); API-SEC-014 read-only listing uses the
 * inherited {@code findAll(Specification, Pageable)}. Permissions are system-generated — no
 * create/update/delete finder is exposed. The pageFk/moduleFk EXACT filters (QR-SEC-0015) are
 * nested association paths, applied by an explicit Specification in PermissionService.
 */
@Repository
public interface PermissionRepository
    extends JpaRepository<Permission, Long>,
            JpaSpecificationExecutor<Permission> {

    /**
     * QR-SEC-0016 generation pre-check (RULE-SEC-010/011) — the permissionCodes already registered
     * for the given page, fed to PermissionGenerationDomainService so a collision throws rather
     * than inserting a duplicate.
     */
    @Query("SELECT p.permissionCode FROM Permission p WHERE p.page.id = :pageId")
    Set<String> findPermissionCodesByPageId(@Param("pageId") Long pageId);
}
