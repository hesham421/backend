package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.web.util.PageableValidator;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.security.entity.Role;
import com.erp.security.dto.CreateRoleRequest;
import com.erp.security.dto.RoleDto;
import com.erp.security.dto.UpdateRoleRequest;
import com.erp.security.exception.SecurityErrorCodes;
import com.erp.security.mapper.RoleMapper;
import com.erp.security.repository.PermissionRepository;
import com.erp.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Contract: role-access.contract.md (BE-REQ-ROLEACCESS-001). */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;

    // Whitelist of allowed sort fields (Rule 17.3)
    private static final Set<String> ALLOWED_ROLE_SORT_FIELDS = Set.of(
        "id", "roleName", "name", "active", "createdAt", "updatedAt"
    );

    // Whitelist of allowed search fields for dynamic filtering
    private static final Set<String> ALLOWED_ROLE_SEARCH_FIELDS = Set.of(
        "roleName", "active"
    );

    // Client-facing sort alias (API contract uses "name" in some clients) -> entity field
    private static final Map<String, String> ROLE_SORT_ALIASES = Map.of(
        "name", "roleName"
    );

    /**
     * Contract: role-access.contract.md - Endpoint 3
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_CREATE)")
    public ServiceResult<RoleDto> createRole(CreateRoleRequest req) {
        String roleCode = req.getRoleCode().toUpperCase().trim();

        // Validate uniqueness of roleCode
        roleRepo.findByRoleCode(roleCode).ifPresent(r -> {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.DUPLICATE_ROLE_CODE, roleCode);
        });

        // Validate uniqueness of roleName
        roleRepo.findByRoleName(req.getRoleName()).ifPresent(r -> {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.DUPLICATE_ROLE_NAME, req.getRoleName());
        });

        Role role = Role.builder()
                .roleCode(roleCode)
                .roleName(req.getRoleName())
                .description(req.getDescription())
                .permissions(new HashSet<>())
                .build();

        Role saved = roleRepo.save(role);
        log.info("Created role '{}' (code: {})", saved.getRoleName(), saved.getRoleCode());
        return ServiceResult.success(RoleMapper.toDto(saved), Status.CREATED);
    }

    /**
     * Contract: role-access.contract.md - Endpoint 1
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_VIEW)")
    public ServiceResult<Page<RoleDto>> listRoles(String search, Boolean active, Pageable pageable) {
        // Validate sort fields (Rule 17.3)
        pageable = PageableValidator.validateSortFields(pageable, ALLOWED_ROLE_SORT_FIELDS, ROLE_SORT_ALIASES);

        Page<Role> roles = roleRepo.findByFilters(search, active, pageable);
        return ServiceResult.success(roles.map(RoleMapper::toDto));
    }

    /**
     * POST /api/roles/search
     * Dynamic search for roles with filtering, sorting, and pagination
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_VIEW)")
    public ServiceResult<Page<RoleDto>> searchRoles(SearchRequest request) {
        // Build JPA Specification from filters
        Specification<Role> spec = SpecBuilder.build(
            request,
            new SetAllowedFields(ALLOWED_ROLE_SEARCH_FIELDS),
            DefaultFieldValueConverter.INSTANCE
        );

        // Build Pageable with validated sort fields
        Pageable pageable = PageableBuilder.from(request, ALLOWED_ROLE_SORT_FIELDS);

        Page<Role> roles = (spec != null) ? roleRepo.findAll(spec, pageable) : roleRepo.findAll(pageable);
        return ServiceResult.success(roles.map(RoleMapper::toDto));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 2
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_VIEW)")
    public ServiceResult<RoleDto> getRoleById(Long id) {
        Role role = roleRepo.findById(id)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, id));
        return ServiceResult.success(RoleMapper.toDto(role));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 4
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
    public ServiceResult<RoleDto> updateRole(Long id, UpdateRoleRequest req) {
        Role role = roleRepo.findById(id)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, id));

        // Check if new roleName conflicts with another role
        roleRepo.findByRoleName(req.getRoleName()).ifPresent(r -> {
            if (!r.getId().equals(id)) {
                throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.DUPLICATE_ROLE_NAME, req.getRoleName());
            }
        });

        // Update fields (roleCode is immutable)
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());

        Role saved = roleRepo.save(role);
        log.info("Updated role '{}' (id: {})", saved.getRoleName(), saved.getId());
        return ServiceResult.success(RoleMapper.toDto(saved), Status.UPDATED);
    }

    /**
     * Contract: role-access.contract.md - Endpoint 5
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_DELETE)")
    public void deleteRole(Long id) {
        // Check if role exists
        Role role = roleRepo.findById(id)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, id));

        // Business Prevention: Check if role has user assignments (409 Conflict)
        if (roleRepo.hasUserAssignments(id)) {
            throw new LocalizedException(Status.CONFLICT, SecurityErrorCodes.ROLE_IN_USE);
        }

        roleRepo.delete(role);
        log.info("Deleted role '{}' (id: {})", role.getRoleName(), id);
    }

    /**
     * Contract: role-access.contract.md - Endpoint 11 (activate)
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
    public ServiceResult<RoleDto> activate(Long id) {
        Role role = roleRepo.findById(id)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, id));

        role.activate();
        Role saved = roleRepo.save(role);

        log.info("Activated role '{}' (id: {})", saved.getRoleName(), saved.getId());
        return ServiceResult.success(RoleMapper.toDto(saved), Status.UPDATED);
    }

    /**
     * Contract: role-access.contract.md - Endpoint 11 (deactivate)
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
    public ServiceResult<RoleDto> deactivate(Long id) {
        Role role = roleRepo.findById(id)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, id));

        role.deactivate();
        Role saved = roleRepo.save(role);

        log.info("Deactivated role '{}' (id: {})", saved.getRoleName(), saved.getId());
        return ServiceResult.success(RoleMapper.toDto(saved), Status.UPDATED);
    }
}
