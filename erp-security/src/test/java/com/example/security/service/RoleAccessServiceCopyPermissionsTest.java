package com.example.security.service;

import com.example.erp.common.exception.LocalizedException;
import com.example.security.dto.CopyPermissionsResponse;
import com.example.security.dto.PermissionType;
import com.example.security.entity.Page;
import com.example.security.entity.Permission;
import com.example.security.entity.Role;
import com.example.security.exception.SecurityErrorCodes;
import com.example.security.repository.PageRepository;
import com.example.security.repository.PermissionRepository;
import com.example.security.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * GAP-SEC-04 (Option A — page-scoped copy only, per architect decision on OQ-SEC-GAP-001).
 */
@ExtendWith(MockitoExtension.class)
class RoleAccessServiceCopyPermissionsTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PageRepository pageRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private RoleAccessService service;

    private Page page;
    private Permission pageViewPermission;
    private Permission systemPermission;

    @BeforeEach
    void setUp() {
        service = new RoleAccessService(roleRepository, pageRepository, permissionRepository);

        page = Page.builder()
                .id(1L)
                .pageCode("USERS")
                .nameEn("Users")
                .nameAr("المستخدمون")
                .route("/users")
                .active(true)
                .build();

        pageViewPermission = Permission.builder()
                .id(10L)
                .name("PERM_USERS_VIEW")
                .page(page)
                .permissionType(PermissionType.VIEW)
                .build();

        systemPermission = Permission.builder()
                .id(20L)
                .name("PERM_SYSTEM_ADMIN")
                .page(null)
                .permissionType(null)
                .build();
    }

    @Test
    void copyPermissionsFromRole_copiesOnlyPageScopedPermissions_leavesTargetSystemPermissionsUntouched() {
        Set<Permission> sourcePermissions = new HashSet<>(Set.of(pageViewPermission, systemPermission));
        Role sourceRole = Role.builder().id(1L).roleName("Source").permissions(sourcePermissions).build();

        Permission targetSystemPermission = Permission.builder()
                .id(30L).name("PERM_GL_JOURNAL_APPROVE").page(null).permissionType(null).build();
        Set<Permission> targetPermissions = new HashSet<>(Set.of(targetSystemPermission));
        Role targetRole = Role.builder().id(2L).roleName("Target").permissions(targetPermissions).build();

        when(roleRepository.findByIdWithPermissions(2L)).thenReturn(Optional.of(targetRole));
        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.of(sourceRole));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.copyPermissionsFromRole(2L, 1L);

        assertThat(targetRole.getPermissions()).contains(pageViewPermission, targetSystemPermission);
        assertThat(targetRole.getPermissions()).doesNotContain(systemPermission);

        CopyPermissionsResponse body = result.getData();
        assertThat(body.getRoleId()).isEqualTo(2L);
        assertThat(body.getCopiedFrom().getRoleId()).isEqualTo(1L);
    }

    @Test
    void copyPermissionsFromRole_sourceHasOnlySystemPermissions_throwsNoPermissionsToCopy() {
        Set<Permission> sourcePermissions = new HashSet<>(Set.of(systemPermission));
        Role sourceRole = Role.builder().id(1L).roleName("Source").permissions(sourcePermissions).build();
        Role targetRole = Role.builder().id(2L).roleName("Target").permissions(new HashSet<>()).build();

        when(roleRepository.findByIdWithPermissions(2L)).thenReturn(Optional.of(targetRole));
        when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.of(sourceRole));

        assertThatThrownBy(() -> service.copyPermissionsFromRole(2L, 1L))
                .isInstanceOf(LocalizedException.class)
                .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                        .isEqualTo(SecurityErrorCodes.NO_PERMISSIONS_TO_COPY));
    }
}
