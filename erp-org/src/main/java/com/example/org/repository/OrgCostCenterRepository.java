package com.example.org.repository;

import com.example.org.domain.OrgCostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgCostCenterRepository
        extends JpaRepository<OrgCostCenter, Long>, JpaSpecificationExecutor<OrgCostCenter> {

    boolean existsByNameArIgnoreCaseAndBranchId(String nameAr, Long branchId);
    boolean existsByNameEnIgnoreCaseAndBranchId(String nameEn, Long branchId);
    boolean existsByNameArIgnoreCaseAndBranchIdAndIdNot(String nameAr, Long branchId, Long id);
    boolean existsByNameEnIgnoreCaseAndBranchIdAndIdNot(String nameEn, Long branchId, Long id);
    boolean existsByCostCenterCode(String costCenterCode);

    @Query("SELECT COUNT(c) FROM OrgCostCenter c WHERE c.parentCostCenter.id = :parentId AND c.isActiveFl = true")
    long countActiveChildren(@Param("parentId") Long parentId);

    @Query("SELECT c FROM OrgCostCenter c WHERE c.branch.id = :branchId AND c.parentCostCenter IS NULL ORDER BY c.nameEn")
    List<OrgCostCenter> findRootsByBranchId(@Param("branchId") Long branchId);

    // QR-ORG-015: flat fetch for in-service recursive tree assembly (API-ORG-027)
    @Query("SELECT c FROM OrgCostCenter c WHERE c.branch.id = :branchId ORDER BY c.nameEn")
    List<OrgCostCenter> findFlatByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT c FROM OrgCostCenter c WHERE c.branch.id = :branchId AND c.isActiveFl = :isActiveFl ORDER BY c.nameEn")
    List<OrgCostCenter> findFlatByBranchIdAndIsActiveFl(@Param("branchId") Long branchId, @Param("isActiveFl") Boolean isActiveFl);
}
