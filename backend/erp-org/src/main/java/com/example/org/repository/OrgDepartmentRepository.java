package com.example.org.repository;

import com.example.org.domain.OrgDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgDepartmentRepository
        extends JpaRepository<OrgDepartment, Long>, JpaSpecificationExecutor<OrgDepartment> {

    boolean existsByNameArIgnoreCaseAndBranchId(String nameAr, Long branchId);
    boolean existsByNameEnIgnoreCaseAndBranchId(String nameEn, Long branchId);
    boolean existsByNameArIgnoreCaseAndBranchIdAndIdNot(String nameAr, Long branchId, Long id);
    boolean existsByNameEnIgnoreCaseAndBranchIdAndIdNot(String nameEn, Long branchId, Long id);
    boolean existsByDepartmentCode(String departmentCode);

    @Query("SELECT COUNT(c) FROM OrgDepartment c WHERE c.parentDepartment.id = :parentId AND c.isActiveFl = true")
    long countActiveChildren(@Param("parentId") Long parentId);

    @Query("SELECT d FROM OrgDepartment d WHERE d.branch.id = :branchId AND d.parentDepartment IS NULL ORDER BY d.nameEn")
    List<OrgDepartment> findRootsByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT d FROM OrgDepartment d JOIN FETCH d.children WHERE d.branch.id = :branchId AND d.parentDepartment IS NULL")
    List<OrgDepartment> findTreeRootsByBranchId(@Param("branchId") Long branchId);
}
