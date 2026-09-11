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
import com.erp.sec.domain.UserDomain;
import com.erp.sec.dto.UserCreateRequest;
import com.erp.sec.dto.UserResponse;
import com.erp.sec.dto.UserSearchRequest;
import com.erp.sec.dto.UserStatusResponse;
import com.erp.sec.dto.UserUpdateRequest;
import com.erp.sec.entity.ActiveSession;
import com.erp.sec.entity.AuditLogEntry;
import com.erp.sec.entity.User;
import com.erp.sec.exception.SecErrorCodes;
import com.erp.sec.mapper.UserMapper;
import com.erp.sec.repository.ActiveSessionRepository;
import com.erp.sec.repository.AuditLogEntryRepository;
import com.erp.sec.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for ENT-SEC-001 (User) — API-SEC-005/006/007/009/010. No caching: SEC is absent from
 * the gov-enforce-caching-rules approved register. The raw password is hashed once here and never
 * logged, stored or returned.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    /** AUDIT_EVENT_TYPE code (CHK_SEC_AUDIT_LOG_EVENT_TYPE) written by the deactivate cascade. */
    private static final String EVENT_SESSION_TERMINATED = "SESSION_TERMINATED";

    /** SCR-REQ-SEC-004 B2 filters (username/email, fullName, statusCode) as entity columns (A.5.6). */
    private static final Set<String> ALLOWED_SORT_FIELDS =
        Set.of("username", "email", "fullNameAr", "fullNameEn", "statusCode");

    private final UserRepository repository;
    private final ActiveSessionRepository activeSessionRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * API-SEC-006. SRS A6 defines no AUDIT_EVENT_TYPE for plain user creation, so no audit row is
     * appended here.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_USERS_CREATE)")
    public ServiceResult<UserResponse> create(UserCreateRequest request) {
        log.info("Creating User with username: {}", request.getUsername());

        boolean usernameTaken = repository.existsByUsername(request.getUsername());
        boolean emailTaken = repository.existsByEmail(request.getEmail());

        UserDomain.create(request.getUsername(), request.getEmail(), usernameTaken, emailTaken);

        User saved = repository.save(
            mapper.toEntity(request, passwordEncoder.encode(request.getPassword())));
        log.info("Created User ID: {}", saved.getUserPk());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    /** API-SEC-007 — username is immutable, so only email uniqueness is re-checked (QR-SEC-033). */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<UserResponse> update(Long id, UserUpdateRequest request) {
        log.info("Updating User ID: {}", id);

        User entity = loadUser(id);

        boolean emailTaken = repository.existsByEmailAndUserPkNot(request.getEmail(), id);
        UserDomain.from(entity).assertEmailAvailable(emailTaken);

        mapper.updateEntityFromRequest(entity, request);
        User saved = repository.save(entity);
        log.info("Updated User ID: {}", saved.getUserPk());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    /**
     * API-SEC-009 — deactivate, then end every still-open session of that user (REQ-SEC-011's
     * second half), one SESSION_TERMINATED audit row per session, all in this transaction.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<UserStatusResponse> deactivate(Long id) {
        log.info("Deactivating User ID: {}", id);

        User entity = loadUser(id);
        entity.deactivate();
        User saved = repository.save(entity);

        String principal = SecurityContextHelper.getCurrentUsername();
        List<ActiveSession> openSessions = activeSessionRepository.findNonTerminatedByUser(id);
        for (ActiveSession session : openSessions) {
            ActiveSessionDomain.from(session).assertCanTerminate();
            session.terminate(principal);
            auditLogEntryRepository.save(AuditLogEntry.builder()
                .eventTypeCode(EVENT_SESSION_TERMINATED)
                .actor(currentActor(principal))
                .occurredAt(Instant.now())
                .targetRef(String.valueOf(session.getActiveSessionPk()))
                .detailsAr("إنهاء الجلسة بسبب تعطيل المستخدم")
                .detailsEn("Session terminated because the user was deactivated")
                .build());
        }
        activeSessionRepository.saveAll(openSessions);
        log.info("Deactivated User ID: {}, terminated sessions: {}", saved.getUserPk(), openSessions.size());

        return ServiceResult.success(mapper.toStatusResponse(saved), Status.UPDATED);
    }

    /** API-SEC-010 — only a DISABLED user may be reactivated (A7 lifecycle, UserDomain decides). */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<UserStatusResponse> reactivate(Long id) {
        log.info("Reactivating User ID: {}", id);

        User entity = loadUser(id);
        UserDomain.from(entity).assertCanReactivate();

        entity.activate();
        User saved = repository.save(entity);
        log.info("Reactivated User ID: {}", saved.getUserPk());

        return ServiceResult.success(mapper.toStatusResponse(saved), Status.UPDATED);
    }

    /** API-SEC-005 — an empty match is success with empty content, never a 404 (CORE search contract). */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_USERS_VIEW)")
    public ServiceResult<Page<UserResponse>> search(UserSearchRequest searchRequest) {
        log.debug("Searching User");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SecSearchSupport.assertSortAllowed(commonRequest.getSortField(), ALLOWED_SORT_FIELDS);

        Specification<User> spec = SpecBuilder.build(commonRequest,
            new SetAllowedFields(ALLOWED_SORT_FIELDS), DefaultFieldValueConverter.INSTANCE);
        String fullName = searchRequest.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            spec = spec.and(fullNameMatches(fullName));
        }

        Page<User> result =
            repository.findAll(spec, PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS));

        return ServiceResult.success(result.map(mapper::toResponse));
    }

    /**
     * {@code fullName} matches either language column (API-SEC-005 Request line); the shared
     * {@code SearchOperator} set has no OR, so this one predicate is expressed directly.
     */
    private Specification<User> fullNameMatches(String fullName) {
        String pattern = "%" + fullName.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("fullNameAr")), pattern),
            cb.like(cb.lower(root.get("fullNameEn")), pattern));
    }

    private User loadUser(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.SEC_404_USER, id));
    }

    /** DBF-SEC-085 is nullable — an actor the token cannot resolve is recorded as null. */
    private User currentActor(String principal) {
        return repository.findByUsername(principal).orElse(null);
    }
}
