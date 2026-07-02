package com.example.erp.org.repository;

import com.example.erp.org.entity.OrgLocationSite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationSiteRepository
    extends JpaRepository<OrgLocationSite, Long>,
            JpaSpecificationExecutor<OrgLocationSite> {

    // RULE-ORG-005 reference check (Branch deactivation guard) — see BranchService.
    // A.2.7 requires JPQL @Query for reference-check count queries.
    @Query("SELECT COUNT(l) FROM OrgLocationSite l WHERE l.branch.id = :branchId AND l.isActiveFl = true")
    long countByBranch_IdAndIsActiveFlTrue(@Param("branchId") Long branchId);

    long countByBranch_Id(Long branchId);

    // A.2.6 — eager-fetches the parent for Response name resolution (SVC-API-HEADER: LEFT JOIN
    // ORG_BRANCH) to avoid N+1 across a search result page.
    @Override
    @EntityGraph(attributePaths = "branch")
    Page<OrgLocationSite> findAll(Specification<OrgLocationSite> spec, Pageable pageable);

    boolean existsByBranch_IdAndLocationSiteCode(Long branchId, String locationSiteCode);

    boolean existsByBranch_IdAndNameAr(Long branchId, String nameAr);

    boolean existsByBranch_IdAndNameEn(Long branchId, String nameEn);

    boolean existsByBranch_IdAndNameArAndIdNot(Long branchId, String nameAr, Long id);

    boolean existsByBranch_IdAndNameEnAndIdNot(Long branchId, String nameEn, Long id);
}
