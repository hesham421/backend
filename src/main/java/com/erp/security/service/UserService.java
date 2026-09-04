package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.PageableBuilder;
import com.erp.common.util.TokenHasher;
import com.erp.security.domain.AccountActivationTokenDomain;
import com.erp.security.domain.UserAccountDomain;
import com.erp.security.dto.UserCreateRequest;
import com.erp.security.dto.UserResponse;
import com.erp.security.dto.UserSearchRequest;
import com.erp.security.dto.UserUpdateRequest;
import com.erp.security.entity.AccountActivationToken;
import com.erp.security.entity.UserAccount;
import com.erp.security.event.AccountActivationRequestedEvent;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.jwt.JwtTokenProvider;
import com.erp.security.mapper.UserMapper;
import com.erp.security.repository.AccountActivationTokenRepository;
import com.erp.security.repository.UserAccountRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for ENTITY-SEC-001 (UserAccount) management — API-SEC-007/008/009/010 (SCR-SEC-001).
 * Every "is this operation allowed?" decision delegates to UserAccountDomain (RULE-SEC-001/002/012);
 * this class loads data, applies decisions, persists, and (on create) issues the activation token
 * and publishes AccountActivationRequestedEvent. No caching (register empty — gov-enforce-caching
 * D.1.1/D.5.5). Role assignment (API-SEC-012) lives in UserRoleService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserAccountRepository repository;
    private final UserMapper mapper;
    private final AccountActivationTokenRepository activationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "username", "email", "userStatusId", "createdAt"
    );

    /**
     * API-SEC-007 — create a user PENDING_ACTIVATION. RULE-SEC-002 (required) + RULE-SEC-001
     * (username/email uniqueness) are decided by UserAccountDomain.create(...). The NOT-NULL
     * PASSWORD_HASH column is satisfied with an unusable placeholder (BCrypt of a fresh random
     * value) so the account cannot authenticate until activation sets a real password; login is
     * additionally blocked for non-ACTIVE status by RULE-SEC-009. An activation token (raw opaque,
     * stored SHA-256-hashed like AUTH's tokens) is issued and its raw value carried on the event.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_CREATE)")
    public ServiceResult<UserResponse> create(UserCreateRequest request) {
        log.info("Creating UserAccount with username: {}", request.getUsername());
        LocalDateTime now = LocalDateTime.now();

        // 1. Fetch what RULE-SEC-001 needs (QR-SEC-0005 / QR-SEC-0006)
        boolean usernameTaken = repository.existsByUsername(request.getUsername());
        boolean emailTaken = repository.existsByEmail(request.getEmail());

        // 2. Delegate the decision (RULE-SEC-002 required + RULE-SEC-001 uniqueness)
        UserAccountDomain.create(request.getUsername(), request.getEmail(), request.getFullName(),
            usernameTaken, emailTaken);

        // 3. Map + set system-managed fields, then persist (QR-SEC-0001; audit via listener)
        UserAccount entity = mapper.toEntity(request);
        entity.setUserStatusId(UserAccountDomain.STATUS_PENDING_ACTIVATION);
        String placeholderRaw = jwtTokenProvider.generateOpaqueToken();
        String placeholderHash = passwordEncoder.encode(placeholderRaw);
        UserAccountDomain.assertStoredHashed(placeholderRaw, placeholderHash); // RULE-SEC-004
        entity.setPasswordHash(placeholderHash);
        UserAccount saved = repository.save(entity);
        log.info("Created UserAccount ID: {}", saved.getId());

        // 4. Issue the activation token (QR-SEC-0021, hashed at rest) and publish the CU event
        String rawToken = jwtTokenProvider.generateOpaqueToken();
        AccountActivationToken token = AccountActivationToken.builder()
            .token(TokenHasher.sha256Hex(rawToken))
            .expiresAt(AccountActivationTokenDomain.issue(now).getExpiresAt())
            .used(false)
            .userAccount(saved)
            .build();
        activationTokenRepository.save(token);

        eventPublisher.publishEvent(new AccountActivationRequestedEvent(
            saved.getId(), saved.getEmail(), rawToken, token.getExpiresAt()));
        log.info("Activation token issued for user ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    /**
     * API-SEC-008 — paged search. username/email are LIKE and userStatusId/isActiveFl EXACT
     * (QR-SEC-0003); they bind from GET params, so an explicit Specification applies them
     * (build-create-service A.5.17) while paging/sort use the shared PageableBuilder against the
     * ALLOWED_SORT_FIELDS whitelist. Empty result → 200 [] (never 404).
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_VIEW)")
    public ServiceResult<Page<UserResponse>> search(UserSearchRequest searchRequest) {
        log.debug("Searching UserAccount");

        Specification<UserAccount> spec = buildSpecification(searchRequest);
        Pageable pageable = PageableBuilder.from(searchRequest.toCommonSearchRequest(), ALLOWED_SORT_FIELDS);

        Page<UserAccount> page = repository.findAll(spec, pageable);

        return ServiceResult.success(page.map(mapper::toResponse));
    }

    /**
     * API-SEC-009 — update. username is immutable (absent from the request). RULE-SEC-001 email
     * uniqueness is re-checked only when the email actually changes; a userStatusId change is
     * validated against the RULE-SEC-012 lifecycle before the mapper applies it.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<UserResponse> update(Long id, UserUpdateRequest request) {
        log.info("Updating UserAccount ID: {}", id);

        UserAccount entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.USER_ACCOUNT_NOT_FOUND, id));

        // RULE-SEC-001 — email uniqueness, only when it changed
        if (!entity.getEmail().equals(request.getEmail())
            && repository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new LocalizedException(
                Status.ALREADY_EXISTS, SecErrorCodes.USER_ACCOUNT_EMAIL_DUPLICATE, request.getEmail());
        }

        // RULE-SEC-012 — validate the lifecycle transition before applying it
        UserAccountDomain.from(entity).assertCanTransitionTo(request.getUserStatusId());

        mapper.updateEntityFromRequest(entity, request);
        UserAccount saved = repository.save(entity);
        log.info("Updated UserAccount ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    /**
     * API-SEC-010 — soft deactivate. Sets isActiveFl = false AND userStatusId = INACTIVE (respecting
     * the RULE-SEC-012 transition). No cascade to SOFT consumers; history retained; reactivation
     * permitted. Returns void so the controller responds 204.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_DELETE)")
    public void deactivate(Long id) {
        log.info("Deactivating UserAccount ID: {}", id);

        UserAccount entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.USER_ACCOUNT_NOT_FOUND, id));

        // RULE-SEC-012 — the ACTIVE/PENDING → INACTIVE transition guard (idempotent if already INACTIVE)
        UserAccountDomain.from(entity).assertCanTransitionTo(UserAccountDomain.STATUS_INACTIVE);

        entity.deactivate();
        entity.setUserStatusId(UserAccountDomain.STATUS_INACTIVE);
        repository.save(entity);
        log.info("Deactivated UserAccount ID: {}", id);
    }

    /**
     * QR-SEC-0003 — explicit Specification for the account's own scalar filters the generic
     * SpecBuilder does not express here: username/email (case-insensitive LIKE) and
     * userStatusId/isActive (EXACT). Empty request → no predicates → returns all.
     */
    private Specification<UserAccount> buildSpecification(UserSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("username")),
                    "%" + request.getUsername().toLowerCase() + "%"));
            }
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                    "%" + request.getEmail().toLowerCase() + "%"));
            }
            if (request.getUserStatusId() != null && !request.getUserStatusId().isBlank()) {
                predicates.add(cb.equal(root.get("userStatusId"), request.getUserStatusId()));
            }
            if (request.getIsActiveFl() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActiveFl()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
