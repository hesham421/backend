package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.domain.AuthorizationGrantDomainService;
import com.erp.security.entity.Permission;
import com.erp.security.entity.RoleModuleId;
import com.erp.security.entity.RolePermission;
import com.erp.security.entity.RolePermissionId;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.repository.PermissionRepository;
import com.erp.security.repository.RoleModuleRepository;
import com.erp.security.repository.RolePermissionRepository;
import com.erp.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for the Tier-2 Role↔Permission grant (API-SEC-015 grant / revoke). Grant enforces
 * RULE-SEC-014 (no orphan screen permission): the role must already hold the module of the
 * permission's page (SEC_PERMISSION.PAGE_FK → SEC_PAGE.MODULE_FK), decided by
 * {@link AuthorizationGrantDomainService}; the grant/revoke themselves are idempotent join
 * insert/delete (QR-SEC-0018). No caching (register empty). Reached via RoleController.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleModuleRepository roleModuleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * API-SEC-015 grant — validate the role and permission exist (absent → ERR-0012 NOT_FOUND),
     * resolve the permission's page's module, then require the role to hold that module
     * (RULE-SEC-014 → QR-SEC-0027 → ERR-0013/422 if not) before an idempotent insert into
     * SEC_ROLE_PERMISSION (QR-SEC-0018): if the grant already exists it is a no-op.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_UPDATE)")
    public ServiceResult<Void> grant(Long roleId, Long permissionId) {
        log.info("Granting Permission ID: {} to Role ID: {}", permissionId, roleId);

        requireRole(roleId);
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.PERMISSION_NOT_FOUND, permissionId));

        Long moduleFk = permission.getPage().getModule().getId();
        boolean roleHoldsPageModule = roleModuleRepository.existsById(new RoleModuleId(roleId, moduleFk));
        AuthorizationGrantDomainService.assertScreenPermissionGrantAllowed(roleHoldsPageModule);

        RolePermissionId id = new RolePermissionId(roleId, permissionId);
        if (!rolePermissionRepository.existsById(id)) {
            rolePermissionRepository.save(RolePermission.builder().id(id).build());
            log.info("Granted Permission ID: {} to Role ID: {}", permissionId, roleId);
        } else {
            log.info("Permission ID: {} already granted to Role ID: {} — idempotent no-op", permissionId, roleId);
        }

        return ServiceResult.success(null);
    }

    /**
     * API-SEC-015 revoke — idempotent delete of the SEC_ROLE_PERMISSION row (QR-SEC-0018). No
     * derivation check on a single-permission revoke (RULE-SEC-014 blocks module revoke, not screen
     * revoke). Absent grant → no-op.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_UPDATE)")
    public ServiceResult<Void> revoke(Long roleId, Long permissionId) {
        log.info("Revoking Permission ID: {} from Role ID: {}", permissionId, roleId);

        RolePermissionId id = new RolePermissionId(roleId, permissionId);
        if (rolePermissionRepository.existsById(id)) {
            rolePermissionRepository.deleteById(id);
            log.info("Revoked Permission ID: {} from Role ID: {}", permissionId, roleId);
        } else {
            log.info("Permission ID: {} not held by Role ID: {} — idempotent no-op", permissionId, roleId);
        }

        return ServiceResult.success(null);
    }

    private void requireRole(Long roleId) {
        roleRepository.findById(roleId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, roleId));
    }
}
