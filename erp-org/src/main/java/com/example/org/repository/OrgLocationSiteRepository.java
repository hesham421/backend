package com.example.org.repository;

import com.example.org.domain.OrgLocationSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgLocationSiteRepository
        extends JpaRepository<OrgLocationSite, Long>, JpaSpecificationExecutor<OrgLocationSite> {

    boolean existsByNameArIgnoreCaseAndBranchId(String nameAr, Long branchId);
    boolean existsByNameEnIgnoreCaseAndBranchId(String nameEn, Long branchId);
    boolean existsByNameArIgnoreCaseAndBranchIdAndIdNot(String nameAr, Long branchId, Long id);
    boolean existsByNameEnIgnoreCaseAndBranchIdAndIdNot(String nameEn, Long branchId, Long id);
    boolean existsByLocationSiteCode(String locationSiteCode);
}
