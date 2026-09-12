package com.erp.fin.repository;

import com.erp.fin.entity.EventTypeRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENT-FIN-009 (EventTypeRule). Module-internal (A.2.3).
 *
 * <p>QR-FIN-012 (search event-type rules, API-FIN-009) is satisfied by the inherited
 * {@code findAll(Specification, Pageable)} plus the shared search layer at the service;
 * QR-FIN-013 (create, API-FIN-010) by the inherited {@code save}.
 */
@Repository
public interface EventTypeRuleRepository
    extends JpaRepository<EventTypeRule, Long>,
            JpaSpecificationExecutor<EventTypeRule> {

    /**
     * QR-FIN-014 — "one active rule per event type" (§6.4, {@code UQ_FIN_EVENT_TYPE_RULE_CODE}).
     * The service passes the result into {@code EventTypeRuleDomain.create(...)}, which owns the
     * decision ({@code FIN-409-RULE-DUP}). No {@code AndIdNot} variant: {@code eventTypeCode} is
     * create-only (A.2.5).
     */
    boolean existsByEventTypeCode(String eventTypeCode);

    /**
     * QR-FIN-027 — RULE-FIN-005's fact at post time (API-FIN-020): the active rule for a given
     * event type, if any. Returns the row rather than a boolean because the same call feeds
     * QR-FIN-025's entry build; absence is turned into {@code FIN-404-NO-ACTIVE-RULE} by the
     * service. {@code eventTypeCode} is unique, so at most one row can match.
     */
    Optional<EventTypeRule> findByEventTypeCodeAndIsActiveFl(String eventTypeCode,
                                                             Boolean isActiveFl);
}
