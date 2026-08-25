package com.erp.org.repository;

import com.erp.org.entity.OrgLegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalEntityRepository
    extends JpaRepository<OrgLegalEntity, Long>,
            JpaSpecificationExecutor<OrgLegalEntity> {

    boolean existsByLegalEntityCode(String legalEntityCode);

    boolean existsByNameAr(String nameAr);

    boolean existsByNameEn(String nameEn);

    boolean existsByNameArAndIdNot(String nameAr, Long id);

    boolean existsByNameEnAndIdNot(String nameEn, Long id);
}
