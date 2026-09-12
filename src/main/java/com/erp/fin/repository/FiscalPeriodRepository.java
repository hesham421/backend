package com.erp.fin.repository;

import com.erp.fin.entity.FiscalPeriod;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENT-FIN-008 (FiscalPeriod). Module-internal (A.2.3).
 *
 * <p>QR-FIN-038 (SAVE with the year) is served by the cascade on {@code FiscalYear.periods};
 * QR-FIN-039 (UPDATE open / soft-close / hard-close, API-FIN-024/025/026) uses the inherited
 * {@code save} after the Domain object has decided. Search is the inherited
 * {@code findAll(Specification, Pageable)} plus the shared {@code SpecBuilder}/
 * {@code PageableBuilder} layer.
 */
@Repository
public interface FiscalPeriodRepository
    extends JpaRepository<FiscalPeriod, Long>,
            JpaSpecificationExecutor<FiscalPeriod> {

    /**
     * QR-FIN-049 — API-FIN-023's response (the year plus its generated periods) and API-FIN-027's year-end
     * close, which transitions every period of the year. {@code JOIN FETCH} on the owning year
     * avoids the N+1 a derived query would cause (A.2.6).
     */
    @Query("SELECT p FROM FiscalPeriod p JOIN FETCH p.fiscalYear "
        + "WHERE p.fiscalYear.fiscalYearPk = :fiscalYearPk ORDER BY p.periodNo")
    List<FiscalPeriod> findByFiscalYearId(@Param("fiscalYearPk") Long fiscalYearPk);
}
