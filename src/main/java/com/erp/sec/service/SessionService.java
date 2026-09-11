package com.erp.sec.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.common.util.SecurityContextHelper;
import com.erp.sec.domain.ActiveSessionDomain;
import com.erp.sec.dto.ActiveSessionResponse;
import com.erp.sec.dto.ActiveSessionSearchRequest;
import com.erp.sec.dto.SessionTerminationResponse;
import com.erp.sec.entity.ActiveSession;
import com.erp.sec.entity.AuditLogEntry;
import com.erp.sec.entity.User;
import com.erp.sec.exception.SecErrorCodes;
import com.erp.sec.mapper.ActiveSessionMapper;
import com.erp.sec.repository.ActiveSessionRepository;
import com.erp.sec.repository.AuditLogEntryRepository;
import com.erp.sec.repository.UserRepository;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for ENT-SEC-010 (ActiveSession) — API-SEC-025/026. Invalidating the session's
 * {@code tokenRef} for subsequent requests is the CORE authorization filter's job (REQ-SEC-028),
 * not this service's.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    /** AUDIT_EVENT_TYPE code (CHK_SEC_AUDIT_LOG_EVENT_TYPE) this API appends. */
    private static final String EVENT_SESSION_TERMINATED = "SESSION_TERMINATED";

    /** SCR-REQ-SEC-009 B2 filters (user, IP address) as entity columns (A.5.6). */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("user", "ipAddress");

    /** {@code user} is an association, so a client-supplied filter may only target the scalar. */
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of("ipAddress");

    private final ActiveSessionRepository repository;
    private final AuditLogEntryRepository auditLogEntryRepository;
    private final UserRepository userRepository;
    private final ActiveSessionMapper mapper;

    /** API-SEC-025 — only sessions whose {@code terminatedAt} is null (REQ-SEC-027). */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_SESSIONS_VIEW)")
    public ServiceResult<Page<ActiveSessionResponse>> search(ActiveSessionSearchRequest searchRequest) {
        log.debug("Searching ActiveSession");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SecSearchSupport.assertSortAllowed(commonRequest.getSortField(), ALLOWED_SORT_FIELDS);

        Specification<ActiveSession> spec = SpecBuilder.build(commonRequest,
            new SetAllowedFields(ALLOWED_FILTER_FIELDS), DefaultFieldValueConverter.INSTANCE);
        Long userId = searchRequest.getUserId();
        if (userId != null) {
            spec = spec.and(belongsTo(userId));
        }
        spec = spec.and(notTerminated());

        Page<ActiveSession> result =
            repository.findAll(spec, PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS));

        return ServiceResult.success(result.map(mapper::toResponse));
    }

    /**
     * REQ-SEC-027's server-side invariant, applied unconditionally: a client cannot switch it off
     * or reach a terminated session through this endpoint. No shared IS_NULL operator exists.
     */
    private Specification<ActiveSession> notTerminated() {
        return (root, query, cb) -> cb.isNull(root.get("terminatedAt"));
    }

    /** DBF-SEC-076 is an association, so the predicate compares a reference, never a raw id. */
    private Specification<ActiveSession> belongsTo(Long userId) {
        User reference = userRepository.getReferenceById(userId);
        return (root, query, cb) -> cb.equal(root.get("user"), reference);
    }

    /** API-SEC-026 — a session already terminated is rejected by {@code ActiveSessionDomain}. */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_SESSIONS_DELETE)")
    public ServiceResult<SessionTerminationResponse> terminate(Long id) {
        log.info("Terminating ActiveSession ID: {}", id);

        ActiveSession entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.SEC_404_SESSION, id));

        ActiveSessionDomain.from(entity).assertCanTerminate();

        String principal = SecurityContextHelper.getCurrentUsername();
        entity.terminate(principal);
        ActiveSession saved = repository.save(entity);

        auditLogEntryRepository.save(AuditLogEntry.builder()
            .eventTypeCode(EVENT_SESSION_TERMINATED)
            .actor(userRepository.findByUsername(principal).orElse(null))
            .occurredAt(Instant.now())
            .targetRef(String.valueOf(saved.getActiveSessionPk()))
            .detailsAr("إنهاء الجلسة بواسطة مسؤول")
            .detailsEn("Session terminated by an administrator")
            .build());
        log.info("Terminated ActiveSession ID: {}", saved.getActiveSessionPk());

        return ServiceResult.success(mapper.toTerminationResponse(saved), Status.UPDATED);
    }
}
