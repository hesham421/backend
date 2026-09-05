package com.erp.mdm.repository;

import com.erp.mdm.dto.LookupValueLite;
import com.erp.mdm.entity.LookupValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-MDM-002 (LookupValue). The (lookupTypeFk, valueCode) composite natural key
 * is immutable (RULE-MDM-003 / RULE-MDM-004), so no {@code existsBy...AndIdNot} variant is
 * provided. The existence check (QR-MDM-0010) navigates the {@code lookupType} association's id.
 * Count / consumption queries are added additively by the SVC-API phase.
 */
@Repository
public interface LookupValueRepository
    extends JpaRepository<LookupValue, Long>,
            JpaSpecificationExecutor<LookupValue> {

    /** QR-MDM-0010 (RULE-MDM-003) — valueCode uniqueness within a type for API-MDM-006 create. */
    boolean existsByLookupTypeIdAndValueCode(Long lookupTypeId, String valueCode);

    /**
     * QR-MDM-0015 (API-MDM-011) — active LookupValues under an active LookupType, resolved by the
     * parent's typeCode natural key (DRV-005 intra-module master↔detail join), as the lean
     * {@link LookupValueLite} projection ordered by sortOrder. Both active flags are filtered so an
     * inactive type yields an empty list; the aliases match the projection getters. valueCode is a
     * stable secondary sort so rows with equal (or NULL) sortOrder keep a deterministic order.
     */
    @Query("""
        SELECT lv.valueCode AS valueCode, lv.nameAr AS nameAr, lv.nameEn AS nameEn, lv.sortOrder AS sortOrder
        FROM LookupValue lv
        JOIN lv.lookupType lt
        WHERE lt.typeCode = :typeCode
          AND lt.isActive = true
          AND lv.isActive = true
        ORDER BY lv.sortOrder, lv.valueCode
        """)
    List<LookupValueLite> findActiveByTypeCode(@Param("typeCode") String typeCode);
}
