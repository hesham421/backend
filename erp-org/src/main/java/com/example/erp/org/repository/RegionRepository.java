package com.example.erp.org.repository;

import com.example.erp.org.entity.OrgRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository
    extends JpaRepository<OrgRegion, Long>,
            JpaSpecificationExecutor<OrgRegion> {

    long countByLegalEntity_Id(Long legalEntityId);

    // A.2.6 — eager-fetches both parents for Response name resolution (SVC-API-HEADER: LEFT JOIN
    // ORG_LEGAL_ENTITY, ORG_REGION_TYPE) to avoid N+1 across a search result page.
    @Override
    @EntityGraph(attributePaths = {"legalEntity", "regionType"})
    Page<OrgRegion> findAll(Specification<OrgRegion> spec, Pageable pageable);

    boolean existsByLegalEntity_IdAndRegionCode(Long legalEntityId, String regionCode);

    boolean existsByLegalEntity_IdAndNameAr(Long legalEntityId, String nameAr);

    boolean existsByLegalEntity_IdAndNameEn(Long legalEntityId, String nameEn);

    boolean existsByLegalEntity_IdAndNameArAndIdNot(Long legalEntityId, String nameAr, Long id);

    boolean existsByLegalEntity_IdAndNameEnAndIdNot(Long legalEntityId, String nameEn, Long id);
}
