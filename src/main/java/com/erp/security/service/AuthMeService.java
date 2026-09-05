package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.util.SecurityContextHelper;
import com.erp.security.dto.MeResponse;
import com.erp.security.entity.Module;
import com.erp.security.entity.Permission;
import com.erp.security.entity.Role;
import com.erp.security.entity.UserAccount;
import com.erp.security.repository.ModuleRepository;
import com.erp.security.repository.PermissionRepository;
import com.erp.security.repository.RoleRepository;
import com.erp.security.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for API-SEC-021 (self identity, roles &amp; granted modules/permissions).
 * Resolves the caller from the JWT principal (subject = username, set by JwtAuthenticationFilter
 * — RULE-SEC-015; no path/query parameter ever identifies the target user) and returns the UNION
 * of Tier-1 module grants and Tier-2 permission grants across all the caller's currently active
 * Roles (RULE-SEC-016 — a UserAccount may hold several simultaneously active Roles). An empty
 * grant set is valid — it simply yields empty arrays, not an error.
 *
 * <p><b>No {@code @PreAuthorize} (justified deviation from build-create-service A.5.2).</b> Same
 * reasoning as {@link DashboardService}: the SVC-API-SESSION spec classes this endpoint as
 * authenticated + self-scoped, requiring no specific CORE-9 authority — it returns only the
 * caller's own identity/grants, keyed by the caller's own principal, so it cannot leak another
 * user's data. Its path is absent from SecurityConfig's public allow-list, so the JWT filter
 * already requires an authenticated principal (401 otherwise, RULE-SEC-015).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthMeService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public ServiceResult<MeResponse> getSelf() {
        String username = SecurityContextHelper.getCurrentUsername();
        log.debug("Resolving self identity for user: {}", username);

        UserAccount account = userAccountRepository.findByUsername(username).orElse(null);
        String fullName = account != null ? account.getFullName() : null;

        List<Role> activeRoles = roleRepository.findActiveRolesByUsername(username);
        List<String> roleCodes = activeRoles.stream().map(Role::getRoleCode).toList();
        List<String> roleNames = activeRoles.stream().map(Role::getNameEn).toList();

        List<String> grantedModules = moduleRepository.findGrantedActiveModulesByUsername(username)
            .stream()
            .map(Module::getModuleCode)
            .toList();

        List<String> grantedPermissions = permissionRepository.findGrantedActivePermissionsByUsername(username)
            .stream()
            .map(Permission::getPermissionCode)
            .toList();

        MeResponse response = MeResponse.builder()
            .username(username)
            .fullName(fullName)
            .roleCodes(roleCodes)
            .roleNames(roleNames)
            .grantedModules(grantedModules)
            .grantedPermissions(grantedPermissions)
            .build();

        return ServiceResult.success(response);
    }
}
