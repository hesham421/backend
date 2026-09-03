package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.UserRole;
import com.erp.security.entity.UserRoleId;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.repository.RoleRepository;
import com.erp.security.repository.UserAccountRepository;
import com.erp.security.repository.UserRoleRepository;
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

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_USERS_UPDATE)")
    public ServiceResult<Void> assign(Long userId, Long roleId) {
        log.info("Assigning Role ID: {} to User ID: {}", roleId, userId);

        if (!userAccountRepository.existsById(userId)) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.USER_ACCOUNT_NOT_FOUND, userId);
        }
        if (!roleRepository.existsById(roleId)) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, roleId);
        }

        UserRoleId id = new UserRoleId(userId, roleId);
        if (!userRoleRepository.existsById(id)) {
            userRoleRepository.save(UserRole.builder().id(id).build());
            log.info("Assigned Role ID: {} to User ID: {}", roleId, userId);
        } else {
            log.info("Role ID: {} already assigned to User ID: {} — idempotent no-op", roleId, userId);
        }

        return ServiceResult.success(null);
    }
}
