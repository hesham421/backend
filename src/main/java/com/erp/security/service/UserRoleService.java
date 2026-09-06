package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.dto.RoleResponse;
import com.erp.security.entity.Role;
import com.erp.security.entity.UserAccount;
import com.erp.security.entity.UserRole;
import com.erp.security.entity.UserRoleId;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.mapper.RoleMapper;
import com.erp.security.repository.RoleRepository;
import com.erp.security.repository.UserAccountRepository;
import com.erp.security.repository.UserRoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for API-SEC-012 (assign role to user). Validates the user and role exist
 * (ERR-0012 NOT_FOUND), then performs an idempotent insert into SEC_USER_ROLE (QR-SEC-0017): if the
 * assignment already exists it is a no-op. No caching (register empty). Reached via UserController.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;

    /**
     * The roles currently assigned to a user — read counterpart of API-SEC-012 assign, used to
     * pre-populate the user drawer's roles picker in edit mode. Validates the user exists
     * (absent → ERR-0012 NOT_FOUND); an empty assignment set is valid → {@code []}.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_VIEW)")
    public ServiceResult<List<RoleResponse>> getRoles(Long userId) {
        log.debug("Fetching assigned roles for User ID: {}", userId);

        if (!userAccountRepository.existsById(userId)) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.USER_ACCOUNT_NOT_FOUND, userId);
        }

        List<RoleResponse> roles = userRoleRepository.findRolesByUserId(userId)
            .stream()
            .map(roleMapper::toResponse)
            .toList();

        return ServiceResult.success(roles);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<Void> assign(Long userId, Long roleId) {
        log.info("Assigning Role ID: {} to User ID: {}", roleId, userId);

        // Active guards mirror the Tier-1 assignModule path: a soft-deactivated user or role is
        // treated as not found, so a stale UI picker cannot grant a dormant role or hang a role off a
        // disabled account. isActive stays TRUE for a PENDING_ACTIVATION user, so roles can still be
        // pre-assigned in the create→activation window.
        requireActiveUser(userId);
        requireActiveRole(roleId);

        UserRoleId id = new UserRoleId(userId, roleId);
        if (!userRoleRepository.existsById(id)) {
            userRoleRepository.save(UserRole.builder().id(id).build());
            log.info("Assigned Role ID: {} to User ID: {}", roleId, userId);
        } else {
            log.info("Role ID: {} already assigned to User ID: {} — idempotent no-op", roleId, userId);
        }

        return ServiceResult.success(null);
    }

    /**
     * Revoke a single role from a user — the counterpart of {@link #assign}, mirroring the Tier-2
     * RolePermissionService.revoke shape: an idempotent delete of the SEC_USER_ROLE row. An absent
     * assignment is a no-op (the role is simply not held). Lets an over-broad assignment be corrected
     * without the collateral damage of deactivating the whole role or the whole user.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<Void> revoke(Long userId, Long roleId) {
        log.info("Revoking Role ID: {} from User ID: {}", roleId, userId);

        UserRoleId id = new UserRoleId(userId, roleId);
        if (userRoleRepository.existsById(id)) {
            userRoleRepository.deleteById(id);
            log.info("Revoked Role ID: {} from User ID: {}", roleId, userId);
        } else {
            log.info("Role ID: {} not assigned to User ID: {} — idempotent no-op", roleId, userId);
        }

        return ServiceResult.success(null);
    }

    /** Existence + active guard for the user (assign path). Inactive is treated as not found. */
    private void requireActiveUser(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.USER_ACCOUNT_NOT_FOUND, userId));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.USER_ACCOUNT_NOT_FOUND, userId);
        }
    }

    /** Existence + active guard for the role (assign path). Inactive is treated as not found. */
    private void requireActiveRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, roleId));
        if (!Boolean.TRUE.equals(role.getIsActive())) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, roleId);
        }
    }
}
