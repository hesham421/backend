package com.example.security.service;

import com.example.erp.common.exception.LocalizedException;
import com.example.security.dto.CreateRoleRequest;
import com.example.security.dto.RoleDto;
import com.example.security.entity.Role;
import com.example.security.exception.SecurityErrorCodes;
import com.example.security.repository.PermissionRepository;
import com.example.security.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GAP: registry-security.md §8.2 — roleCode/description are now persisted (no longer @Transient).
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepo;
    @Mock
    private PermissionRepository permRepo;

    private RoleService service;

    @BeforeEach
    void setUp() {
        service = new RoleService(roleRepo, permRepo);
    }

    @Test
    void createRole_persistsRoleCodeAndDescription_andRoundTripsThroughDto() {
        CreateRoleRequest req = CreateRoleRequest.builder()
                .roleCode("finance_mgr")
                .roleName("Finance Manager")
                .description("Manages finance module")
                .active(true)
                .build();

        when(roleRepo.findByRoleCode("FINANCE_MGR")).thenReturn(Optional.empty());
        when(roleRepo.findByRoleName("Finance Manager")).thenReturn(Optional.empty());
        when(roleRepo.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });

        RoleDto dto = service.createRole(req).getData();

        assertThat(dto.getRoleCode()).isEqualTo("FINANCE_MGR");
        assertThat(dto.getRoleName()).isEqualTo("Finance Manager");
        assertThat(dto.getDescription()).isEqualTo("Manages finance module");
    }

    @Test
    void createRole_duplicateRoleCode_throwsDuplicateRoleCode() {
        CreateRoleRequest req = CreateRoleRequest.builder()
                .roleCode("ADMIN")
                .roleName("Administrator")
                .build();

        Role existing = Role.builder().id(1L).roleCode("ADMIN").roleName("Admin").build();
        when(roleRepo.findByRoleCode("ADMIN")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createRole(req))
                .isInstanceOf(LocalizedException.class)
                .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                        .isEqualTo(SecurityErrorCodes.DUPLICATE_ROLE_CODE));

        verify(roleRepo, never()).save(any(Role.class));
    }
}
