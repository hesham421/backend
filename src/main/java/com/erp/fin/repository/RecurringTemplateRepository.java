package com.erp.fin.repository;

import com.erp.fin.entity.RecurringTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENT-FIN-011 (RecurringTemplate). Module-internal (A.2.3).
 *
 * <p>QR-FIN-017 (search templates, API-FIN-012) is satisfied by the inherited
 * {@code findAll(Specification, Pageable)} plus the shared search layer at the service;
 * QR-FIN-018 (create template with lines, API-FIN-013) by the inherited {@code save}, with the
 * lines saved through {@code RecurringTemplateLineRepository} in the same transaction.
 * QR-FIN-019's single-template load (API-FIN-014) uses the inherited {@code findById}.
 */
@Repository
public interface RecurringTemplateRepository
    extends JpaRepository<RecurringTemplate, Long>,
            JpaSpecificationExecutor<RecurringTemplate> {
}
