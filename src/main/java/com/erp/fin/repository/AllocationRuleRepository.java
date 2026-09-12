package com.erp.fin.repository;

import com.erp.fin.entity.AllocationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENT-FIN-013 (AllocationRule). Module-internal (A.2.3).
 *
 * <p>QR-FIN-020 (search allocation rules, API-FIN-015) is satisfied by the inherited
 * {@code findAll(Specification, Pageable)} plus the shared search layer at the service;
 * QR-FIN-021 (create rule with targets, API-FIN-016) by the inherited {@code save}, with the
 * targets saved through {@code AllocationTargetRepository} in the same transaction; QR-FIN-022's
 * single-rule load (API-FIN-017) by the inherited {@code findById}. No bespoke method is
 * declared — adding one with no caller would be dead code (A.2.9).
 */
@Repository
public interface AllocationRuleRepository
    extends JpaRepository<AllocationRule, Long>,
            JpaSpecificationExecutor<AllocationRule> {
}
