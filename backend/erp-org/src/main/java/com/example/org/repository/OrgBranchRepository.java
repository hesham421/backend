package com.example.org.repository;

import com.example.org.domain.OrgBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgBranchRepository
        extends JpaRepository<OrgBranch, Long>, JpaSpecificationExecutor<OrgBranch> {

    boolean existsByNameArIgnoreCaseAndLegalEntityId(String nameAr, Long legalEntityId);
    boolean existsByNameEnIgnoreCaseAndLegalEntityId(String nameEn, Long legalEntityId);
    boolean existsByNameArIgnoreCaseAndLegalEntityIdAndIdNot(String nameAr, Long legalEntityId, Long id);
    boolean existsByNameEnIgnoreCaseAndLegalEntityIdAndIdNot(String nameEn, Long legalEntityId, Long id);
    boolean existsByBranchCode(String branchCode);

    @Query("SELECT COUNT(d) FROM OrgDepartment d WHERE d.branch.id = :branchId AND d.isActiveFl = true")
    long countActiveDepartments(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(cc) FROM OrgCostCenter cc WHERE cc.branch.id = :branchId AND cc.isActiveFl = true")
    long countActiveCostCenters(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(ls) FROM OrgLocationSite ls WHERE ls.branch.id = :branchId AND ls.isActiveFl = true")
    long countActiveLocationSites(@Param("branchId") Long branchId);
}
