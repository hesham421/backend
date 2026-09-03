package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.util.SecurityContextHelper;
import com.erp.security.dto.ModuleResponse;
import com.erp.security.mapper.ModuleMapper;
import com.erp.security.repository.ModuleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for API-SEC-019 (dashboard modules for the current user). Resolves the caller from
 * the JWT principal (subject = username, set by JwtAuthenticationFilter) and returns the distinct
 * ACTIVE modules granted to any of the caller's roles (QR-SEC-0028 → RULE-SEC-013 display filter).
 * An empty result is valid → {@code []}.
 *
 * <p><b>No {@code @PreAuthorize} (justified deviation from build-create-service A.5.2).</b> The
 * SVC-API-MODULES spec classes this endpoint as authenticated + self-scoped, requiring no specific
 * CORE-9 authority: it returns only the caller's own granted modules, keyed by the caller's own
 * principal, so it cannot leak another user's data. Its path is absent from SecurityConfig's public
 * allow-list, so the JWT filter already requires an authenticated principal. This mirrors the
 * reasoning LookupService documents for its authenticated-only endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper mapper;

    @Transactional(readOnly = true)
    public ServiceResult<List<ModuleResponse>> grantedModules() {
        String username = SecurityContextHelper.getCurrentUsername();
        log.debug("Resolving dashboard modules for user: {}", username);

        List<ModuleResponse> modules = moduleRepository.findGrantedActiveModulesByUsername(username)
            .stream()
            .map(mapper::toResponse)
            .toList();

        return ServiceResult.success(modules);
    }
}
