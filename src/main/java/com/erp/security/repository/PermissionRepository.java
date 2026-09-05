package com.erp.security.repository;

import com.erp.security.entity.Permission;
import java.util.List;
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

    /**
     * QR-SEC-0031 (API-SEC-021/022 self) — the distinct ACTIVE Permissions granted to a user
     * through any of its ACTIVE roles (Tier-2 union, RULE-SEC-016/017). SEC_USER_ACCOUNT →
     * SEC_USER_ROLE → SEC_ROLE → SEC_ROLE_PERMISSION → SEC_PERMISSION. The role.isActive /
     * permission.isActive guards mirror QR-SEC-0028 so a deactivated role or permission never
     * counts as currently granted.
     */
    @Query("""
        SELECT DISTINCT p
        FROM Permission p, RolePermission rp, Role r, UserRole ur, UserAccount u
        WHERE u.username = :username
          AND ur.id.userAccountFk = u.id
          AND r.id = ur.id.roleFk
          AND r.isActive = true
          AND rp.id.roleFk = ur.id.roleFk
          AND p.id = rp.id.permissionFk
          AND p.isActive = true
        ORDER BY p.permissionCode
        """)
    List<Permission> findGrantedActivePermissionsByUsername(@Param("username") String username);
}
