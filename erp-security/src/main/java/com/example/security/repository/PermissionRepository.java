package com.example.security.repository;

import com.example.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    /**
     * Find all permissions by names
     */
    List<Permission> findByNameIn(List<String> names);

    /**
     * Find all permissions for a specific page (using Spring Data property traversal)
     */
    List<Permission> findByPage_Id(Long pageId);

    /**
     * Find VIEW permissions for a set of page IDs (to check which pages are assigned to roles)
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "p.page.id IN :pageIds AND p.permissionType = 'VIEW'")
    List<Permission> findViewPermissionsByPageIds(
        @Param("pageIds") Set<Long> pageIds
    );

    /**
     * Find all page permissions assigned to a role (via ROLE_PERMISSIONS join table)
     * Returns permissions with their linked pages in single query - optimal for ERP
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN FETCH p.page pg " +
           "JOIN p.id pId " +
           "WHERE p.page IS NOT NULL " +
           "AND p.id IN :permissionIds")
    List<Permission> findPagePermissionsWithPagesByIds(
        @Param("permissionIds") Set<Long> permissionIds
    );
}
