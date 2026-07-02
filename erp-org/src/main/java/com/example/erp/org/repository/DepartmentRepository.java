package com.example.erp.org.repository;

import com.example.erp.org.entity.OrgDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository
    extends JpaRepository<OrgDepartment, Long>,
            JpaSpecificationExecutor<OrgDepartment> {

    // RULE-ORG-003 reference check (Branch deactivation guard) — see BranchService.
    // A.2.7 requires JPQL @Query for reference-check count queries.
    @Query("SELECT COUNT(d) FROM OrgDepartment d WHERE d.branch.id = :branchId AND d.isActiveFl = true")
    long countByBranch_IdAndIsActiveFlTrue(@Param("branchId") Long branchId);

    /**
     * API-ORG-020 — full subtree for a Branch (QR-ORG-012 recursive CTE, PostgreSQL syntax).
     * Returns the flat row set; the Service assembles the nested tree DTO (recursive assembly
     * happens in Java, not the DB — per SVC-API-TREE.md). {@code isActiveFl} is nullable
     * (pass {@code null} for "all") — when provided, filters both the anchor and every
     * recursive step so the whole subtree is scoped to matching nodes.
     */
    @Query(value = """
        WITH RECURSIVE dept_tree AS (
            SELECT * FROM ORG_DEPARTMENT
            WHERE PARENT_DEPARTMENT_FK IS NULL AND BRANCH_FK = :branchFk
              AND (CAST(:isActiveFl AS INTEGER) IS NULL OR IS_ACTIVE_FL = CAST(:isActiveFl AS INTEGER))
            UNION ALL
            SELECT d.* FROM ORG_DEPARTMENT d
            JOIN dept_tree t ON d.PARENT_DEPARTMENT_FK = t.DEPARTMENT_PK
            WHERE (CAST(:isActiveFl AS INTEGER) IS NULL OR d.IS_ACTIVE_FL = CAST(:isActiveFl AS INTEGER))
        )
        SELECT * FROM dept_tree ORDER BY DEPARTMENT_PK
        """, nativeQuery = true)
    List<OrgDepartment> findTreeByBranch(@Param("branchFk") Long branchFk, @Param("isActiveFl") Integer isActiveFl);

    long countByBranch_Id(Long branchId);

    boolean existsByBranch_IdAndDepartmentCode(Long branchId, String departmentCode);

    boolean existsByBranch_IdAndNameAr(Long branchId, String nameAr);

    boolean existsByBranch_IdAndNameEn(Long branchId, String nameEn);

    boolean existsByBranch_IdAndNameArAndIdNot(Long branchId, String nameAr, Long id);

    boolean existsByBranch_IdAndNameEnAndIdNot(Long branchId, String nameEn, Long id);
}
