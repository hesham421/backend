package com.example.org.repository;

import com.example.org.domain.OrgLegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgLegalEntityRepository
        extends JpaRepository<OrgLegalEntity, Long>, JpaSpecificationExecutor<OrgLegalEntity> {

    boolean existsByNameArIgnoreCase(String nameAr);
    boolean existsByNameEnIgnoreCase(String nameEn);
    boolean existsByNameArIgnoreCaseAndIdNot(String nameAr, Long id);
    boolean existsByNameEnIgnoreCaseAndIdNot(String nameEn, Long id);
    boolean existsByLegalEntityCode(String legalEntityCode);

    @Query("SELECT COUNT(b) FROM OrgBranch b WHERE b.legalEntity.id = :leId AND b.isActiveFl = true")
    long countActiveBranches(@Param("leId") Long leId);

    @Query("SELECT COUNT(pc) FROM OrgProfitCenter pc WHERE pc.legalEntity.id = :leId AND pc.isActiveFl = true")
    long countActiveProfitCenters(@Param("leId") Long leId);
}
