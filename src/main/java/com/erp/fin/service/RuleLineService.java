package com.erp.fin.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.fin.domain.EventTypeRuleDomain;
import com.erp.fin.dto.RuleLineCreateRequest;
import com.erp.fin.dto.RuleLineResponse;
import com.erp.fin.entity.EventTypeRule;
import com.erp.fin.entity.RuleLine;
import com.erp.fin.exception.FinErrorCodes;
import com.erp.fin.mapper.RuleLineMapper;
import com.erp.fin.repository.EventTypeRuleRepository;
import com.erp.fin.repository.RuleLineRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration layer for ENT-FIN-010 (RuleLine) — API-FIN-011 (SVC-API-CRUD.md). The endpoint is
 * a method on {@code EventTypeRuleController} (A.6.9); only the service is separate.
 *
 * <p>No caching annotations — FIN's approved cache register is empty. ENT-FIN-010 has no search
 * API, so no {@code ALLOWED_SORT_FIELDS} whitelist applies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleLineService {

    private final RuleLineRepository repository;
    private final EventTypeRuleRepository eventTypeRuleRepository;
    private final RuleLineMapper mapper;
    private final FinLookupValidationService lookupValidation;

    /**
     * API-FIN-011 — resolve the parent rule ({@code FIN-404-RULE}), validate all four
     * lookup-backed codes against MDL (XM-FIN-001), then delegate RULE-FIN-003 to
     * {@link EventTypeRuleDomain#assertRemainderLineSetValid(List)}: the decision is taken over
     * the rule's *prospective* line set — QR-FIN-016's already-persisted siblings plus the
     * submitted line — which this service assembles and passes in as a plain argument
     * ({@code FIN-409-REMAINDER-COUNT}).
     *
     * <p>{@code lineNo} (DBF-FIN-100) is not part of the request: it is assigned as the next
     * position within the rule, derived from the sibling set already fetched for the rule check.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants)"
        + ".PERM_FIN_RULES_UPDATE)")
    public ServiceResult<RuleLineResponse> create(Long eventTypeRuleId,
                                                   RuleLineCreateRequest request) {
        log.info("Adding RuleLine to EventTypeRule ID: {}", eventTypeRuleId);

        EventTypeRule parent = eventTypeRuleRepository.findById(eventTypeRuleId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FinErrorCodes.FIN_404_RULE, eventTypeRuleId));

        lookupValidation.assertValidCode(FinLookupValidationService.KEY_ACCOUNT_DERIVATION_TYPE,
            request.getAccountDerivationTypeCode());
        lookupValidation.assertValidCode(FinLookupValidationService.KEY_AMOUNT_SOURCE_TYPE,
            request.getAmountSourceTypeCode());
        lookupValidation.assertValidCode(FinLookupValidationService.KEY_DEBIT_CREDIT,
            request.getDirectionCode());
        lookupValidation.assertValidCode(FinLookupValidationService.KEY_DISTRIBUTION_TYPE,
            request.getDistributionTypeCode());

        List<RuleLine> existingLines = repository.findByEventTypeRulePk(eventTypeRuleId);
        RuleLine newLine = mapper.toEntity(request, parent, existingLines.size() + 1);

        List<RuleLine> prospectiveLines = new ArrayList<>(existingLines);
        prospectiveLines.add(newLine);
        EventTypeRuleDomain.from(parent).assertRemainderLineSetValid(prospectiveLines);

        RuleLine saved = repository.save(newLine);
        log.info("Added RuleLine ID: {} to EventTypeRule ID: {}",
            saved.getRuleLinePk(), eventTypeRuleId);

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }
}
