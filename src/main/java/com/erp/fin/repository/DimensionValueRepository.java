package com.erp.fin.repository;

import com.erp.fin.entity.DimensionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENT-FIN-003 (DimensionValue). Module-internal (A.2.3).
 *
 * <p>QR-FIN-011 (search dimension values, API-FIN-008) is satisfied by the inherited
 * {@code findAll(Specification, Pageable)} plus the shared {@code SpecBuilder}/
 * {@code PageableBuilder} layer at the service (filtering by {@code dimension.dimensionPk} and
 * {@code code}). QR-FIN-009 (create, API-FIN-007) uses the inherited {@code save}.
 */
@Repository
public interface DimensionValueRepository
    extends JpaRepository<DimensionValue, Long>,
            JpaSpecificationExecutor<DimensionValue> {

    /**
     * QR-FIN-010 — RULE-FIN-002's fact and API-FIN-007's duplicate pre-check
     * ({@code UQ_FIN_DIMENSION_VALUE_DIM_CODE}). The service passes the result into
     * {@code DimensionValueDomain.create(...)}, which owns the decision. No {@code AndIdNot}
     * variant: {@code code} is create-only, so the update-time exclusion check would be dead
     * code (A.2.5).
     */
    boolean existsByDimension_DimensionPkAndCode(Long dimensionPk, String code);
}
