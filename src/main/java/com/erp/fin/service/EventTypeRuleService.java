package com.erp.fin.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.fin.domain.EventTypeRuleDomain;
import com.erp.fin.dto.EventTypeRuleCreateRequest;
import com.erp.fin.dto.EventTypeRuleSearchRequest;
import com.erp.fin.dto.EventTypeRuleResponse;
import com.erp.fin.entity.EventTypeRule;
import com.erp.fin.mapper.EventTypeRuleMapper;
import com.erp.fin.repository.EventTypeRuleRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration layer for ENT-FIN-009 (EventTypeRule) — API-FIN-010 (SVC-API-CRUD.md).
 *
 * <p>Precondition recorded by the spec, not enforced per request: this endpoint is only reachable
 * once FIN's onboarding has completed — FIN's screens/actions registered into SEC (REQ-FIN-044)
 * and FIN's 13 lookup types, {@code ACCOUNTING_EVENT_TYPE} among them, registered into MDL
 * (REQ-FIN-045). Both run once at deployment.
 *
 * <p>No caching annotations — FIN's approved cache register is empty. No
 * {@code ALLOWED_SORT_FIELDS} is present because SVC-API-SEARCH added API-FIN-009 here (A.5.6).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventTypeRuleService {

    /**
     * A.5.6 — API-FIN-009's filter/sort whitelist: the plan's {@code eventTypeCode} and
     * {@code isActiveFl} filters, plus ENT-FIN-009's remaining flat columns for ordering.
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "eventTypeRulePk", "eventTypeCode", "nameAr", "nameEn", "isActiveFl", "createdAt");

    private final EventTypeRuleRepository repository;
    private final EventTypeRuleMapper mapper;
    private final FinLookupValidationService lookupValidation;

    /**
     * API-FIN-010 — validate {@code eventTypeCode} against MDL (XM-FIN-001), ask QR-FIN-014
     * whether the event type already carries a rule, then delegate the §6.4 one-rule-per-type
     * decision to {@link EventTypeRuleDomain#create(String, boolean)}
     * ({@code FIN-409-RULE-DUP}).
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_RULES_CREATE)")
    public ServiceResult<EventTypeRuleResponse> create(EventTypeRuleCreateRequest request) {
        log.info("Creating EventTypeRule for event type: {}", request.getEventTypeCode());

        lookupValidation.assertValidCode(
            FinLookupValidationService.KEY_ACCOUNTING_EVENT_TYPE, request.getEventTypeCode());

        boolean ruleAlreadyExists = repository.existsByEventTypeCode(request.getEventTypeCode());

        EventTypeRuleDomain.create(request.getEventTypeCode(), ruleAlreadyExists);

        EventTypeRule saved = repository.save(mapper.toEntity(request));
        log.info("Created EventTypeRule ID: {}", saved.getEventTypeRulePk());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    /**
     * API-FIN-009 — QR-FIN-012, criteria search over ENT-FIN-009, no join, read-only.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_RULES_VIEW)")
    public ServiceResult<Page<EventTypeRuleResponse>> search(
            EventTypeRuleSearchRequest searchRequest) {
        log.debug("Searching EventTypeRule");

        FinSearchSupport.assertSortAllowed(searchRequest.getSortField(), ALLOWED_SORT_FIELDS);

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<EventTypeRule> spec = SpecBuilder.build(
            commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        return ServiceResult.success(repository.findAll(spec, pageable).map(mapper::toResponse));
    }
}
