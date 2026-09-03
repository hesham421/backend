package com.erp.security.repository;

import com.erp.security.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-004 (Page, screen registry / CORE-9 owner). pageCode is the immutable
 * natural key (RULE-SEC-010), so no {@code existsBy...AndIdNot} variant is provided — it can never
 * change on update. Pages are addressed by surrogate id (API-SEC-013 {@code /pages/{id}}), so
 * uniqueness is served by {@code existsByPageCode} alone. The moduleFk EXACT filter (QR-SEC-0012)
 * is a nested association path, so it is applied by an explicit Specification in PageService rather
 * than a derived method here.
 */
@Repository
public interface PageRepository
    extends JpaRepository<Page, Long>,
            JpaSpecificationExecutor<Page> {

    /** QR-SEC-0014 (RULE-SEC-010) — pageCode uniqueness pre-check for API-SEC-013 create. */
    boolean existsByPageCode(String pageCode);
}
