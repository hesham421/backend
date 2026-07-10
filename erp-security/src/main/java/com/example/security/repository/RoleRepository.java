package com.example.security.repository;

import com.example.security.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Role entity
 *
 * Governance: BE-REQ-ROLEACCESS-001
 * Contract: role-access.contract.md
 *
 * <h2>Active Flag Query Pattern:</h2>
 * When the IS_ACTIVE column is added to ROLES table, use this pattern:
 * <pre>
 * (:isActive IS NULL OR r.active = :isActive)
 * </pre>
 *
 * This allows:
 * - isActive = null → returns ALL roles
 * - isActive = true → returns only active roles
 * - isActive = false → returns only inactive roles
 */
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    // Find by roleName (unique)
    Optional<Role> findByRoleName(String roleName);

    // Find by roleCode (unique)
    Optional<Role> findByRoleCode(String roleCode);

    // Legacy support - maps to roleName
    @Deprecated
    default Optional<Role> findByName(String name) {
        return findByRoleName(name);
    }

    // Fetch join to avoid N+1 when loading role with permissions AND their pages
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions p LEFT JOIN FETCH p.page WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    /**
     * Paginated search with filters per contract.
     *
     * @param search optional search term for role name
     * @param active optional active filter (null = ALL, true = active only, false = inactive only)
     * @param pageable pagination info
     * @return paginated roles matching criteria
     */
    @Query("SELECT r FROM Role r WHERE " +
           "(:search IS NULL OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:active IS NULL OR r.active = :active)")
    Page<Role> findByFilters(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable);

    // Check if role has any user assignments (for delete validation)
    @Query("SELECT COUNT(u) > 0 FROM UserAccount u JOIN u.roles r WHERE r.id = :roleId")
    boolean hasUserAssignments(@Param("roleId") Long roleId);

    /**
     * Find all active roles.
     */
    List<Role> findByActiveTrue();
}
