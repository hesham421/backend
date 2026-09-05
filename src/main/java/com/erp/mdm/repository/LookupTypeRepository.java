package com.erp.mdm.repository;

import com.erp.mdm.entity.LookupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-MDM-001 (LookupType). typeCode is the immutable natural key
 * (RULE-MDM-002), so no {@code existsBy...AndIdNot} variant is provided — it can never change on
 * update. Uniqueness (RULE-MDM-001 / QR-MDM-0003) is served by {@code existsByTypeCode} alone.
 * Count / consumption queries are added additively by the SVC-API phase.
 */
@Repository
public interface LookupTypeRepository
    extends JpaRepository<LookupType, Long>,
            JpaSpecificationExecutor<LookupType> {

    /** QR-MDM-0003 (RULE-MDM-001) — typeCode uniqueness pre-check for API-MDM-001 create. */
    boolean existsByTypeCode(String typeCode);

    /** QR-MDM-0006 (RULE-MDM-006) — active LookupValue usage count for the API-MDM-004 deactivate guard. */
    @Query("SELECT COUNT(v) FROM LookupValue v WHERE v.lookupType.id = :typeId AND v.isActive = true")
    long countActiveValuesByType(@Param("typeId") Long typeId);
}
