package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.domain.AuthorizationGrantDomainService;
import com.erp.security.dto.ModuleResponse;
import com.erp.security.entity.Module;
import com.erp.security.entity.Role;
import com.erp.security.entity.RoleModule;
import com.erp.security.entity.RoleModuleId;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.mapper.ModuleMapper;
import com.erp.security.repository.ModuleRepository;
import com.erp.security.repository.RoleModuleRepository;
import com.erp.security.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for the Tier-1 Role↔Module grant (API-SEC-017 assign / API-SEC-018 revoke). Loads
 * the existence facts the RULE-SEC-013/014 invariants need and delegates the revoke-block decision
 * to {@link AuthorizationGrantDomainService}; the grant itself is a plain idempotent join
 * insert/delete (QR-SEC-0026). No caching (register empty). Reached via RoleController.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleModuleService {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final RoleModuleRepository roleModuleRepository;
    private final ModuleMapper moduleMapper;

    /**
     * The Tier-1 modules currently granted to a role — read counterpart of assign/revoke, used to
     * pre-populate the role screen's module picker in edit mode. Validates the role exists
     * (absent → ERR-0012 NOT_FOUND); an empty grant set is valid → {@code []}.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_VIEW)")
    public ServiceResult<List<ModuleResponse>> getModules(Long roleId) {
        log.debug("Fetching granted modules for Role ID: {}", roleId);
        requireRole(roleId);

        List<ModuleResponse> modules = roleModuleRepository.findModulesByRoleId(roleId)
            .stream()
            .map(moduleMapper::toResponse)
            .toList();

        return ServiceResult.success(modules);
    }

    /**
     * API-SEC-017 — grant a module to a role. Validates the role and module exist and are active
     * (inactive → ERR-0012 NOT_FOUND), then performs an idempotent insert into SEC_ROLE_MODULE
     * (QR-SEC-0026): if the grant already exists it is a no-op. A Tier-1 grant is a dashboard
     * display filter + prerequisite (RULE-SEC-013) — no module-level runtime gate is created.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_UPDATE)")
    public ServiceResult<Void> assignModule(Long roleId, Long moduleId) {
        log.info("Assigning Module ID: {} to Role ID: {}", moduleId, roleId);

        requireActiveRole(roleId);
        requireActiveModule(moduleId);

        RoleModuleId id = new RoleModuleId(roleId, moduleId);
        if (!roleModuleRepository.existsById(id)) {
            roleModuleRepository.save(RoleModule.builder().id(id).build());
            log.info("Granted Module ID: {} to Role ID: {}", moduleId, roleId);
        } else {
            log.info("Module ID: {} already granted to Role ID: {} — idempotent no-op", moduleId, roleId);
        }

        return ServiceResult.success(null);
    }

    /**
     * API-SEC-018 — revoke a module from a role. Validates the role, module and grant exist
     * (absent → ERR-0012 NOT_FOUND), then blocks the revoke if the role still holds any screen
     * permission for a page in this module (QR-SEC-0029 → RULE-SEC-014, ERR-0014 → 409, decided by
     * {@link AuthorizationGrantDomainService}). Otherwise deletes the SEC_ROLE_MODULE row.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_UPDATE)")
    public ServiceResult<Void> revokeModule(Long roleId, Long moduleId) {
        log.info("Revoking Module ID: {} from Role ID: {}", moduleId, roleId);

        requireRole(roleId);
        requireModule(moduleId);

        RoleModuleId id = new RoleModuleId(roleId, moduleId);
        if (!roleModuleRepository.existsById(id)) {
            // Grant absent → the module is not granted to the role (ERR-0012, resource not found).
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, moduleId);
        }

        boolean roleStillHoldsScreenPermissionsInModule =
            roleModuleRepository.existsRolePermissionForRoleInModule(roleId, moduleId);
        AuthorizationGrantDomainService.assertModuleRevokeAllowed(roleStillHoldsScreenPermissionsInModule);

        roleModuleRepository.deleteById(id);
        log.info("Revoked Module ID: {} from Role ID: {}", moduleId, roleId);

        return ServiceResult.success(null);
    }

    /** Existence + active guard for a role (assign path). Inactive is treated as not found. */
    private void requireActiveRole(Long roleId) {
        Role role = requireRole(roleId);
        if (!Boolean.TRUE.equals(role.getIsActive())) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, roleId);
        }
    }

    /** Existence + active guard for a module (assign path). Inactive is treated as not found. */
    private void requireActiveModule(Long moduleId) {
        Module module = requireModule(moduleId);
        if (!Boolean.TRUE.equals(module.getIsActive())) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, moduleId);
        }
    }

    private Role requireRole(Long roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, roleId));
    }

    private Module requireModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, moduleId));
    }
}
