package com.example.erp.org.repository;

import com.example.erp.org.entity.OrgCostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostCenterRepository
    extends JpaRepository<OrgCostCenter, Long>,
            JpaSpecificationExecutor<OrgCostCenter> {

    // RULE-ORG-004 reference check (Branch deactivation guard) — see BranchService.
    // A.2.7 requires JPQL @Query for reference-check count queries.
    @Query("SELECT COUNT(c) FROM OrgCostCenter c WHERE c.branch.id = :branchId AND c.isActiveFl = true")
    long countByBranch_IdAndIsActiveFlTrue(@Param("branchId") Long branchId);

    /**
     * API-ORG-027 — full subtree for a Branch (QR-ORG-015 recursive CTE, PostgreSQL syntax,
     * mirrors QR-ORG-012). Returns the flat row set; the Service assembles the nested tree DTO.
     * {@code isActiveFl} is nullable (pass {@code null} for "all") — when provided, filters both
     * the anchor and every recursive step so the whole subtree is scoped to matching nodes.
     */
    @Query(value = """
        WITH RECURSIVE cc_tree AS (
            SELECT * FROM ORG_COST_CENTER
            WHERE PARENT_COST_CENTER_FK IS NULL AND BRANCH_FK = :branchFk
              AND (CAST(:isActiveFl AS INTEGER) IS NULL OR IS_ACTIVE_FL = CAST(:isActiveFl AS INTEGER))
            UNION ALL
            SELECT c.* FROM ORG_COST_CENTER c
            JOIN cc_tree t ON c.PARENT_COST_CENTER_FK = t.COST_CENTER_PK
            WHERE (CAST(:isActiveFl AS INTEGER) IS NULL OR c.IS_ACTIVE_FL = CAST(:isActiveFl AS INTEGER))
        )
        SELECT * FROM cc_tree ORDER BY COST_CENTER_PK
        """, nativeQuery = true)
    List<OrgCostCenter> findTreeByBranch(@Param("branchFk") Long branchFk, @Param("isActiveFl") Integer isActiveFl);

    long countByBranch_Id(Long branchId);

    boolean existsByBranch_IdAndCostCenterCode(Long branchId, String costCenterCode);

    boolean existsByBranch_IdAndNameAr(Long branchId, String nameAr);

    boolean existsByBranch_IdAndNameEn(Long branchId, String nameEn);

    boolean existsByBranch_IdAndNameArAndIdNot(Long branchId, String nameAr, Long id);

    boolean existsByBranch_IdAndNameEnAndIdNot(Long branchId, String nameEn, Long id);
}
