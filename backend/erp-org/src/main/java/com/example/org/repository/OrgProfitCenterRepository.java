package com.example.org.repository;

import com.example.org.domain.OrgProfitCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgProfitCenterRepository
        extends JpaRepository<OrgProfitCenter, Long>, JpaSpecificationExecutor<OrgProfitCenter> {

    boolean existsByNameArIgnoreCaseAndLegalEntityId(String nameAr, Long legalEntityId);
    boolean existsByNameEnIgnoreCaseAndLegalEntityId(String nameEn, Long legalEntityId);
    boolean existsByNameArIgnoreCaseAndLegalEntityIdAndIdNot(String nameAr, Long legalEntityId, Long id);
    boolean existsByNameEnIgnoreCaseAndLegalEntityIdAndIdNot(String nameEn, Long legalEntityId, Long id);
    boolean existsByProfitCenterCode(String profitCenterCode);
}
