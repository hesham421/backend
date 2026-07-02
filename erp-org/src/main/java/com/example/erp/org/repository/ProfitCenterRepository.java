package com.example.erp.org.repository;

import com.example.erp.org.entity.OrgProfitCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfitCenterRepository
    extends JpaRepository<OrgProfitCenter, Long>,
            JpaSpecificationExecutor<OrgProfitCenter> {

    // RULE-ORG-002 reference check (LegalEntity deactivation guard) — see LegalEntityService.
    // A.2.7 requires JPQL @Query for reference-check count queries.
    @Query("SELECT COUNT(p) FROM OrgProfitCenter p WHERE p.legalEntity.id = :legalEntityId AND p.isActiveFl = true")
    long countByLegalEntity_IdAndIsActiveFlTrue(@Param("legalEntityId") Long legalEntityId);

    long countByLegalEntity_Id(Long legalEntityId);

    // A.2.6 — eager-fetches the parent for Response name resolution (SVC-API-HEADER: LEFT JOIN
    // ORG_LEGAL_ENTITY) to avoid N+1 across a search result page.
    @Override
    @EntityGraph(attributePaths = "legalEntity")
    Page<OrgProfitCenter> findAll(Specification<OrgProfitCenter> spec, Pageable pageable);

    boolean existsByLegalEntity_IdAndProfitCenterCode(Long legalEntityId, String profitCenterCode);

    boolean existsByLegalEntity_IdAndNameAr(Long legalEntityId, String nameAr);

    boolean existsByLegalEntity_IdAndNameEn(Long legalEntityId, String nameEn);

    boolean existsByLegalEntity_IdAndNameArAndIdNot(Long legalEntityId, String nameAr, Long id);

    boolean existsByLegalEntity_IdAndNameEnAndIdNot(Long legalEntityId, String nameEn, Long id);
}
