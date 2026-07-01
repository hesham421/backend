package com.example.security.repository;

import com.example.security.entity.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for Page entity
 */
public interface PageRepository extends JpaRepository<Page, Long>, JpaSpecificationExecutor<Page> {

    /**
     * Find page by code
     */
    Optional<Page> findByPageCode(String pageCode);

    /**
     * Check if page code exists
     */
    boolean existsByPageCode(String pageCode);

    /**
     * Check if route exists
     */
    boolean existsByRoute(String route);

    /**
     * Check if route exists excluding specific page ID
     */
    boolean existsByRouteAndIdNot(String route, Long id);

    /**
     * Find all active pages ordered by display order
     */
    List<Page> findByActiveOrderByDisplayOrder(Boolean active);

    /**
     * Find pages by codes (for optimized batch loading)
     * This eliminates N+1 query problem in getRolePages
     */
    @Query("SELECT p FROM Page p WHERE p.pageCode IN :pageCodes")
    List<Page> findByPageCodeIn(@Param("pageCodes") Set<String> pageCodes);

    /**
     * Find active pages by page codes and active status (for menu building)
     */
    @Query("SELECT p FROM Page p WHERE p.pageCode IN :pageCodes AND p.active = :active ORDER BY p.displayOrder")
    List<Page> findByPageCodeInAndActive(
        @Param("pageCodes") Set<String> pageCodes,
        @Param("active") Boolean active
    );

    /**
     * Find active pages by IDs and active status (uses FK from PERMISSIONS)
     * This method is used by MenuService to resolve pages using PAGE_ID_FK from PERMISSIONS table.
     * This approach survives page code renaming and is more resilient.
     */
    @Query("SELECT p FROM Page p WHERE p.id IN :pageIds AND p.active = :active ORDER BY p.displayOrder")
    List<Page> findByIdInAndActive(
        @Param("pageIds") Set<Long> pageIds,
        @Param("active") Boolean active
    );

    /**
     * Find pages by module with pagination
     */
    org.springframework.data.domain.Page<Page> findByModule(String module, Pageable pageable);

    /**
     * Find pages by active status with pagination
     */
    org.springframework.data.domain.Page<Page> findByActive(Boolean active, Pageable pageable);

    /**
     * Search pages by name (Arabic or English) - case insensitive
     */
    @Query("SELECT p FROM Page p WHERE " +
           "(LOWER(p.nameAr) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.pageCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<Page> searchPages(
        @Param("search") String search,
        Pageable pageable
    );
}
