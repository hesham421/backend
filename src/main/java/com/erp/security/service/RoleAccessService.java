package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.constants.SecurityPermissions;
import com.erp.security.entity.Page;
import com.erp.security.entity.Permission;
import com.erp.security.entity.Role;
import com.erp.security.dto.*;
import com.erp.security.exception.SecurityErrorCodes;
import com.erp.security.repository.PageRepository;
import com.erp.security.repository.PermissionRepository;
import com.erp.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages Role-Page assignments. VIEW is always added when a page is assigned to a role and
 * can't be removed while the page stays assigned; VIEW itself is never returned in responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAccessService {

    private final RoleRepository roleRepository;
    private final PageRepository pageRepository;
    private final PermissionRepository permissionRepository;

    /** Contract: role-access.contract.md - Endpoint 6 (VIEW excluded from the permissions array). */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_VIEW)")
    public ServiceResult<RolePagesMatrixResponse> getRolePages(Long roleId) {
        // Fetch role with permissions
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, roleId));

        List<PageAssignmentResponse> assignments = buildPageAssignments(role);

        return ServiceResult.success(RolePagesMatrixResponse.builder()
                .roleId(role.getId())
                .roleName(role.getRoleName())
                .assignments(assignments)
                .build());
    }

    /** Contract: role-access.contract.md - Endpoint 7. VIEW is always added automatically. */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
    public ServiceResult<PageAssignmentResponse> addPageToRole(Long roleId, AddPageToRoleRequest request) {
        // Normalize pageCode
        String pageCode = request.getPageCode().toUpperCase().trim();
        log.info("Adding page '{}' to role ID: {}", pageCode, roleId);

        // Fetch role with permissions
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, roleId));

        // Verify page exists
        Page page = pageRepository.findByPageCode(pageCode)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PAGE_NOT_FOUND_BY_CODE, pageCode));

        // Check for duplicate assignment (page already assigned to role)
        boolean hasViewForPage = role.getPermissions().stream()
            .anyMatch(p -> p.getPage() != null
                && Objects.equals(p.getPage().getId(), page.getId())
                && p.getPermissionType() == PermissionType.VIEW);
        if (hasViewForPage) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.PAGE_ALREADY_ASSIGNED_TO_ROLE, pageCode, role.getRoleName());
        }

        // Validate permission types
        List<String> validCrudPermissions = new ArrayList<>();
        for (String permType : request.getPermissions()) {
            String upperPermType = permType.toUpperCase().trim();
            if (!upperPermType.equals("CREATE") && !upperPermType.equals("UPDATE") && !upperPermType.equals("DELETE")) {
                throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_PERMISSION_TYPE, permType);
            }
            validCrudPermissions.add(upperPermType);
        }

        // Build required permission types: VIEW + requested CRUD
        EnumSet<PermissionType> requiredTypes = EnumSet.of(PermissionType.VIEW);
        for (String permType : validCrudPermissions) {
            PermissionType type = PermissionType.valueOf(permType);
            requiredTypes.add(type);
        }

        // Resolve permissions by page FK + type to avoid hard dependency on name conventions.
        List<Permission> permissions = resolvePagePermissions(page, requiredTypes);

        // Add permissions to role
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);

        log.info("Added page '{}' to role '{}' with permissions: {}",
                pageCode, role.getRoleName(), validCrudPermissions);

        // Build response (VIEW excluded per contract)
        return ServiceResult.success(PageAssignmentResponse.builder()
                .pageCode(pageCode)
                .pageName(page.getNameEn())
                .pageNameAr(page.getNameAr())
                .permissions(validCrudPermissions)
                .build(), Status.CREATED);
    }

    /**
     * Contract: role-access.contract.md - Endpoint 8. FULL REPLACE: an empty assignments array
     * removes all page access for this role.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
    public ServiceResult<RolePagesMatrixResponse> syncRolePages(Long roleId, SyncRolePagesRequest request) {
        log.info("Syncing pages for role ID: {} with {} assignments", roleId, request.getAssignments().size());

        // Fetch role with permissions
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, roleId));

        // Build target permission set by page FK + type
        Set<Permission> targetPermissionSet = new LinkedHashSet<>();

        for (SyncRolePagesRequest.PageAssignmentDto assignment : request.getAssignments()) {
            String pageCode = assignment.getPageCode().toUpperCase().trim();

            // Verify page exists
            Page page = pageRepository.findByPageCode(pageCode)
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PAGE_NOT_FOUND_BY_CODE, pageCode));

            EnumSet<PermissionType> requiredTypes = EnumSet.of(PermissionType.VIEW);

            // Validate and add requested CRUD permissions
            for (String permType : assignment.getPermissions()) {
                String upperPermType = permType.toUpperCase().trim();
                if (!upperPermType.equals("CREATE") && !upperPermType.equals("UPDATE") && !upperPermType.equals("DELETE")) {
                    throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_PERMISSION_TYPE, permType);
                }
                PermissionType type = PermissionType.valueOf(upperPermType);
                requiredTypes.add(type);
            }

            targetPermissionSet.addAll(resolvePagePermissions(page, requiredTypes));
        }

        List<Permission> targetPermissions = new ArrayList<>(targetPermissionSet);

        // Get current page-related permissions for this role, matched by page FK
        // (not by name prefix — a page-linked permission isn't guaranteed to
        // follow the PERM_<CODE>_<TYPE> naming convention, e.g. when created
        // directly via POST /api/permissions).
        Set<Permission> currentPagePermissions = role.getPermissions().stream()
                .filter(Permission::isPagePermission)
                .collect(Collectors.toSet());

        // Remove all current page permissions
        role.getPermissions().removeAll(currentPagePermissions);

        // Add new target permissions
        role.getPermissions().addAll(targetPermissions);

        roleRepository.save(role);

        log.info("Synced role '{}': removed {} old page permissions, added {} new permissions",
                role.getRoleName(), currentPagePermissions.size(), targetPermissions.size());

        // Build response
        List<PageAssignmentResponse> assignments = buildPageAssignments(role);

        return ServiceResult.success(RolePagesMatrixResponse.builder()
                .roleId(role.getId())
                .roleName(role.getRoleName())
                .assignments(assignments)
                .build(), Status.UPDATED);
    }

    private List<Permission> resolvePagePermissions(Page page, EnumSet<PermissionType> requiredTypes) {
        List<Permission> pagePermissions = permissionRepository.findByPage_Id(page.getId());

        Map<PermissionType, Permission> byType = new EnumMap<>(PermissionType.class);
        for (Permission permission : pagePermissions) {
            if (permission.getPermissionType() != null) {
                byType.put(permission.getPermissionType(), permission);
            }
        }

        if (!byType.keySet().containsAll(requiredTypes)) {
            throw new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PERMISSIONS_NOT_FOUND);
        }

        List<Permission> resolved = new ArrayList<>(requiredTypes.size());
        for (PermissionType type : requiredTypes) {
            resolved.add(byType.get(type));
        }
        return resolved;
    }

    /** Contract: role-access.contract.md - Endpoint 9. Removes VIEW + all CRUD for that page. */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
    public void removePageFromRole(Long roleId, String pageCode) {
        // Normalize pageCode
        final String normalizedPageCode = pageCode.toUpperCase().trim();
        pageCode = normalizedPageCode;
        log.info("Removing page '{}' from role ID: {}", pageCode, roleId);

        // Fetch role with permissions
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, roleId));

        // Verify page exists
        Page page = pageRepository.findByPageCode(normalizedPageCode)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PAGE_NOT_FOUND_BY_CODE, normalizedPageCode));

        // Check if page is assigned (has VIEW permission) — matched by page FK +
        // type, same as addPageToRole/resolvePagePermissions, not by name string:
        // a permission linked to this page can exist under a non-canonical name
        // (e.g. created directly via POST /api/permissions), and name-matching
        // would then miss it.
        boolean hasViewForPage = role.getPermissions().stream()
                .anyMatch(p -> p.getPage() != null && Objects.equals(p.getPage().getId(), page.getId())
                        && p.getPermissionType() == PermissionType.VIEW);
        if (!hasViewForPage) {
            throw new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PAGE_NOT_ASSIGNED_TO_ROLE, pageCode);
        }

        // Remove all permissions tied to this page (VIEW + CRUD), regardless of name
        int beforeSize = role.getPermissions().size();
        role.getPermissions().removeIf(p -> p.getPage() != null && Objects.equals(p.getPage().getId(), page.getId()));
        int afterSize = role.getPermissions().size();

        roleRepository.save(role);

        log.info("Removed page '{}' from role '{}' (removed {} permissions)",
                pageCode, role.getRoleName(), (beforeSize - afterSize));
    }

    /**
     * Contract: role-access.contract.md - Endpoint 10. Only page-scoped permissions are copied;
     * the target's system-level permissions (e.g. PERM_SYSTEM_ADMIN) are left untouched, to
     * avoid silently escalating privileges the source role happens to hold.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
    public ServiceResult<CopyPermissionsResponse> copyPermissionsFromRole(Long targetRoleId, Long sourceRoleId) {
        log.info("Copying permissions from role ID: {} to role ID: {}", sourceRoleId, targetRoleId);

        if (targetRoleId.equals(sourceRoleId)) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_OPERATION, "Cannot copy permissions from a role to itself");
        }

        // Fetch both roles with permissions
        Role targetRole = roleRepository.findByIdWithPermissions(targetRoleId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, targetRoleId));

        Role sourceRole = roleRepository.findByIdWithPermissions(sourceRoleId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, sourceRoleId));

        // Get only page-scoped permissions from source role (system-level permissions excluded)
        Set<Permission> sourcePagePermissions = sourceRole.getPermissions().stream()
                .filter(Permission::isPagePermission)
                .collect(Collectors.toSet());

        if (sourcePagePermissions.isEmpty()) {
            throw new LocalizedException(Status.CONFLICT, SecurityErrorCodes.NO_PERMISSIONS_TO_COPY);
        }

        // Remove target's current page-scoped permissions only; its system-level permissions are preserved
        Set<Permission> currentTargetPagePermissions = targetRole.getPermissions().stream()
                .filter(Permission::isPagePermission)
                .collect(Collectors.toSet());

        targetRole.getPermissions().removeAll(currentTargetPagePermissions);

        // Add source's page-scoped permissions to target
        targetRole.getPermissions().addAll(sourcePagePermissions);

        roleRepository.save(targetRole);

        log.info("Copied {} page permissions from role '{}' to role '{}' (replaced {} existing)",
                sourcePagePermissions.size(), sourceRole.getRoleName(), targetRole.getRoleName(),
                currentTargetPagePermissions.size());

        // Build response
        List<PageAssignmentResponse> assignments = buildPageAssignments(targetRole);

        return ServiceResult.success(CopyPermissionsResponse.builder()
                .roleId(targetRole.getId())
                .roleName(targetRole.getRoleName())
                .copiedFrom(CopyPermissionsResponse.SourceRoleInfo.builder()
                        .roleId(sourceRole.getId())
                        .roleName(sourceRole.getRoleName())
                        .build())
                .assignments(assignments)
                .build());
    }

    /** Excludes VIEW from the permissions array per contract. */
    private List<PageAssignmentResponse> buildPageAssignments(Role role) {
        // Group permissions by page - using the direct FK relationship
        Map<Long, Page> pageMap = new HashMap<>();
        Map<Long, EnumSet<PermissionType>> pagePermissionTypes = new HashMap<>();

        for (Permission perm : role.getPermissions()) {
            // Skip non-page permissions (system permissions)
            if (!perm.isPagePermission()) {
                continue;
            }

            Page page = perm.getPage();
            Long pageId = page.getId();
            PermissionType permType = perm.getPermissionType();

            // Store page reference (avoiding duplicate lookups)
            pageMap.putIfAbsent(pageId, page);

            // Track permissions for this page
            pagePermissionTypes
                .computeIfAbsent(pageId, k -> EnumSet.noneOf(PermissionType.class))
                .add(permType);
        }

        // Build response DTOs - only for pages that have VIEW permission (assigned)
        List<PageAssignmentResponse> result = new ArrayList<>(pageMap.size());

        for (Map.Entry<Long, Page> entry : pageMap.entrySet()) {
            Long pageId = entry.getKey();
            Page page = entry.getValue();
            EnumSet<PermissionType> permTypes = pagePermissionTypes.get(pageId);

            // Only include pages that have VIEW permission (means they are assigned)
            if (permTypes == null || !permTypes.contains(PermissionType.VIEW)) {
                continue;
            }

            // Build CRUD permissions list (VIEW excluded per contract)
            List<String> crudPermissions = new ArrayList<>(3);
            if (permTypes.contains(PermissionType.CREATE)) crudPermissions.add("CREATE");
            if (permTypes.contains(PermissionType.UPDATE)) crudPermissions.add("UPDATE");
            if (permTypes.contains(PermissionType.DELETE)) crudPermissions.add("DELETE");

            result.add(PageAssignmentResponse.builder()
                    .pageCode(page.getPageCode())
                    .pageName(page.getNameEn())
                    .pageNameAr(page.getNameAr())
                    .permissions(crudPermissions)
                    .build());
        }

        // Sort by page code for consistent ordering
        result.sort(Comparator.comparing(PageAssignmentResponse::getPageCode));

        return result;
    }
}
