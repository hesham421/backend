package com.erp.sec.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.SecurityContextHelper;
import com.erp.sec.domain.SignupRequestDomain;
import com.erp.sec.dto.SignupDecisionRequest;
import com.erp.sec.dto.SignupRequestResponse;
import com.erp.sec.dto.SignupSubmitRequest;
import com.erp.sec.entity.SignupRequest;
import com.erp.sec.entity.User;
import com.erp.sec.exception.SecErrorCodes;
import com.erp.sec.mapper.SignupRequestMapper;
import com.erp.sec.mapper.UserMapper;
import com.erp.sec.repository.SignupRequestRepository;
import com.erp.sec.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for ENT-SEC-013 (SignupRequest) — API-SEC-011. The response type differs per
 * decision (the created {@code UserResponse} on APPROVE, the updated {@code SignupRequestResponse}
 * on REJECT), which is why the result payload is not a single fixed DTO type.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignupRequestService {

    /** SIGNUP_STATUS code (CHK_SEC_SIGNUP_REQUEST_STATUS) an outstanding request carries. */
    private static final String STATUS_PENDING = "PENDING";

    private final SignupRequestRepository repository;
    private final UserRepository userRepository;
    private final SignupRequestMapper mapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * API-SEC-002 — pre-authentication by contract (SVC-API-INT.md {@code Security : screen
     * SEC_SIGNUP · public — no permission required}), hence no {@code @PreAuthorize} gate. The
     * email must be free of both an existing User and an already-PENDING request; that single fact
     * is judged by {@code SignupRequestDomain.create}.
     */
    @Transactional
    public ServiceResult<SignupRequestResponse> submit(SignupSubmitRequest request) {
        log.info("Submitting SignupRequest for email");

        boolean emailTaken = userRepository.existsByEmail(request.getEmail())
            || repository.existsByEmailAndStatusCode(request.getEmail(), STATUS_PENDING);

        SignupRequestDomain.create(request.getEmail(), emailTaken);

        SignupRequest saved = repository.save(mapper.toEntity(request));
        log.info("Created SignupRequest ID: {}", saved.getSignupRequestPk());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    /**
     * API-SEC-011. SRS A6 defines no AUDIT_EVENT_TYPE for a sign-up decision, so no audit row is
     * appended. On APPROVE the account is created with an unusable random secret — it has no
     * password until its owner completes API-SEC-003/004, and none is transmitted anywhere.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<Object> decide(Long id, SignupDecisionRequest request) {
        log.info("Deciding SignupRequest ID: {}, decision: {}", id, request.getDecision());

        SignupRequest entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.SEC_404_SIGNUP, id));

        SignupRequestDomain.from(entity).assertCanDecide();

        String principal = SecurityContextHelper.getCurrentUsername();

        if (SignupDecisionRequest.DECISION_APPROVE.equals(request.getDecision())) {
            User created = userRepository.save(
                userMapper.toEntity(entity, passwordEncoder.encode(UUID.randomUUID().toString())));
            entity.approve(principal);
            repository.save(entity);
            log.info("Approved SignupRequest ID: {}, created User ID: {}", id, created.getUserPk());

            return ServiceResult.success(userMapper.toResponse(created), Status.UPDATED);
        }

        entity.reject(principal);
        SignupRequest saved = repository.save(entity);
        log.info("Rejected SignupRequest ID: {}", id);

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }
}
