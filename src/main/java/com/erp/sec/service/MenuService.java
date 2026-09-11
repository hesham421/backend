package com.erp.sec.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.util.SecurityContextHelper;
import com.erp.sec.dto.ModuleMenuResponse;
import com.erp.sec.dto.ScreenMenuResponse;
import com.erp.sec.entity.User;
import com.erp.sec.mapper.ModuleRegistryMapper;
import com.erp.sec.mapper.ScreenRegistryMapper;
import com.erp.sec.repository.ModuleRegistryRepository;
import com.erp.sec.repository.RoleActionGrantRepository;
import com.erp.sec.repository.ScreenRegistryRepository;
import com.erp.sec.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API-SEC-027 and the single effective-grant read path CORE.md's REQ-SEC-033 paragraph names.
 * API-SEC-022's per-widget filter consumes {@link #effectivePermissionCodes()} rather than
 * repeating the query. No caching: SEC is absent from the approved register.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final UserRepository userRepository;
    private final ModuleRegistryRepository moduleRepository;
    private final ScreenRegistryRepository screenRepository;
    private final RoleActionGrantRepository roleActionGrantRepository;
    private final ModuleRegistryMapper moduleMapper;
    private final ScreenRegistryMapper screenMapper;

    /**
     * API-SEC-027. Gated on authentication alone: SRS B4 gives this component no page code, so its
     * content — not a permission on itself — is the security boundary.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<List<ModuleMenuResponse>> effective() {
        log.debug("Resolving the effective menu of the authenticated caller");

        Optional<User> caller = resolveCaller();
        if (caller.isEmpty()) {
            return ServiceResult.success(List.of());
        }
        Long userPk = caller.get().getUserPk();

        Map<Long, List<ScreenMenuResponse>> screensByModule =
            screenRepository.findEffectiveScreensForUser(userPk).stream()
                .collect(Collectors.groupingBy(screen -> screen.getModule().getModuleRegPk(),
                    Collectors.mapping(screenMapper::toMenuResponse, Collectors.toList())));

        List<ModuleMenuResponse> menu = moduleRepository.findEffectiveModulesForUser(userPk).stream()
            .map(module -> moduleMapper.toMenuResponse(module, screensByModule.getOrDefault(
                module.getModuleRegPk(), List.of())))
            .toList();

        return ServiceResult.success(menu);
    }

    /** The permission-code shape of the same read path, consumed by API-SEC-022 (REQ-SEC-023). */
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<Set<String>> effectivePermissionCodes() {
        log.debug("Resolving the effective permission codes of the authenticated caller");

        Set<String> codes = resolveCaller()
            .map(caller -> Set.copyOf(
                roleActionGrantRepository.findEffectivePermissionCodesForUser(caller.getUserPk())))
            .orElseGet(Set::of);

        return ServiceResult.success(codes);
    }

    /**
     * Becomes correct the moment SEC-BE installs the JWT validating filter; until then
     * {@code getCurrentUsername()} falls back to "system" — no SEC_USER row — so both callers
     * degrade to an empty result rather than a 404 (both APIs list SEC-500 only).
     */
    private Optional<User> resolveCaller() {
        return userRepository.findByUsername(SecurityContextHelper.getCurrentUsername());
    }
}
