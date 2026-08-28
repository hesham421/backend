package com.erp.security.repository;

import com.erp.security.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    /** @param active null returns all roles, true/false filters to active or inactive only. */
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
