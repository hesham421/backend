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
import com.erp.security.constants.SecurityPermissions;
import com.erp.security.entity.Page;
import com.erp.security.entity.Permission;
import com.erp.security.dto.*;
import com.erp.security.exception.SecurityErrorCodes;
import com.erp.security.repository.PageRepository;
import com.erp.security.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Manages the UI Page registry and creates permission RECORDS (PERM_&lt;CODE&gt;_VIEW/CREATE/UPDATE/DELETE)
 * for each page. Never assigns permissions to a role — that happens only in RoleAccessService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PageService {

    private final PageRepository pageRepository;
    private final PermissionRepository permissionRepository;

    // Whitelist of allowed sort fields (Rule 17.3)
    private static final Set<String> ALLOWED_PAGE_SORT_FIELDS = Set.of(
        "id", "pageCode", "nameAr", "nameEn", "module", "displayOrder", "createdAt", "updatedAt"
    );

    // Whitelist of allowed search fields for dynamic filtering
    private static final Set<String> ALLOWED_PAGE_SEARCH_FIELDS = Set.of(
        "pageCode", "nameAr", "nameEn", "module", "active"
    );

    /**
     * Creates the page plus its 4 permission records; assignment to a role happens separately
     * via RoleAccessService.addPageToRole().
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_CREATE)")
    public ServiceResult<PageResponse> createPage(CreatePageRequest request) {
        // Normalize pageCode to uppercase
        String pageCode = request.getPageCode().toUpperCase().trim();
        log.info("Creating page '{}'", pageCode);

        // ENHANCED VALIDATION: Check pageCode format (must be alphanumeric + underscore only)
        if (!pageCode.matches("^[A-Z0-9_]+$")) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_PAGE_CODE_FORMAT, pageCode);
        }

        // ENHANCED VALIDATION: pageCode length (between 2 and 50 characters)
        if (pageCode.length() < 2 || pageCode.length() > 50) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_PAGE_CODE_LENGTH, pageCode.length());
        }

        // Check for duplicate pageCode
        if (pageRepository.existsByPageCode(pageCode)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.DUPLICATE_PAGE_CODE, pageCode);
        }

        // Validate route format
        String route = request.getRoute().trim();
        validateRouteFormat(route);

        // Check for duplicate route
        if (pageRepository.existsByRoute(route)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.DUPLICATE_ROUTE, route);
        }

        // ENHANCED VALIDATION: Validate parent page exists if parentId is provided
        if (request.getParentId() != null) {
            pageRepository.findById(request.getParentId())
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PARENT_PAGE_NOT_FOUND, request.getParentId()));
        }

        // Create Page entity
        Page page = Page.builder()
                .pageCode(pageCode)
                .nameAr(request.getNameAr())
                .nameEn(request.getNameEn())
                .route(route)
                .icon(request.getIcon())
                .module(request.getModule())
                .parentId(request.getParentId())
                .displayOrder(request.getDisplayOrder())
                .active(request.getActive() != null ? request.getActive() : true)
                .description(request.getDescription())
                .build();

        Set<PermissionType> suppressTypes = request.getSuppressPermissionTypes() != null
                ? request.getSuppressPermissionTypes() : Set.of();
        if (suppressTypes.contains(PermissionType.VIEW)) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.CANNOT_REMOVE_VIEW_PERMISSION);
        }

        Page savedPage = pageRepository.save(page);
        log.info("Page created with ID: {}", savedPage.getId());

        // Create permission RECORDS linked to the page (definitions only, no role assignment)
        Map<String, String> permissionKeys = createPermissionRecords(savedPage, suppressTypes);
        log.info("Auto-generated {} permission records for page: {}", permissionKeys.size(), pageCode);

        return ServiceResult.success(toResponse(savedPage, permissionKeys), Status.CREATED);
    }

    /**
     * Updates a page; pageCode is immutable and cannot be changed here.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_UPDATE)")
    public ServiceResult<PageResponse> updatePage(Long id, UpdatePageRequest request) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PAGE_NOT_FOUND, id));

        log.info("Updating page ID: {} (code: {})", id, page.getPageCode());

        // Validate route format
        String route = request.getRoute().trim();
        validateRouteFormat(route);

        // Check route uniqueness (excluding this page)
        if (pageRepository.existsByRouteAndIdNot(route, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.DUPLICATE_ROUTE, route);
        }

        // ENHANCED VALIDATION: Validate parent page exists if parentId is provided
        if (request.getParentId() != null) {
            // Prevent self-reference
            if (request.getParentId().equals(id)) {
                throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_PARENT_PAGE);
            }

            pageRepository.findById(request.getParentId())
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PARENT_PAGE_NOT_FOUND, request.getParentId()));
        }

        // Update fields
        page.setNameAr(request.getNameAr());
        page.setNameEn(request.getNameEn());
        page.setRoute(route);
        page.setIcon(request.getIcon());
        page.setModule(request.getModule());
        page.setParentId(request.getParentId());
        page.setDisplayOrder(request.getDisplayOrder());
        page.setDescription(request.getDescription());

        Page updated = pageRepository.save(page);
        log.info("Page updated successfully: {}", id);

        // Get existing permission keys
        Map<String, String> permissionKeys = buildPermissionKeys(page.getPageCode());

        return ServiceResult.success(toResponse(updated, permissionKeys), Status.UPDATED);
    }

    /**
     * Get Page by ID
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_VIEW)")
    public ServiceResult<PageResponse> getPageById(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PAGE_NOT_FOUND, id));

        Map<String, String> permissionKeys = buildPermissionKeys(page.getPageCode());
        return ServiceResult.success(toResponse(page, permissionKeys));
    }

    /**
     * List all Pages with pagination
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_VIEW)")
    public ServiceResult<org.springframework.data.domain.Page<PageResponse>> listPages(
            String module,
            Boolean active,
            String search,
            Pageable pageable
    ) {
        // Validate sort fields (Rule 17.3)
        pageable = PageableValidator.validateSortFields(pageable, ALLOWED_PAGE_SORT_FIELDS);

        org.springframework.data.domain.Page<Page> pages;

        if (search != null && !search.trim().isEmpty()) {
            // Search by name or code
            pages = pageRepository.searchPages(search.trim(), pageable);
        } else if (module != null && !module.trim().isEmpty()) {
            // Filter by module
            pages = pageRepository.findByModule(module, pageable);
        } else if (active != null) {
            // Filter by active status
            pages = pageRepository.findByActive(active, pageable);
        } else {
            // All pages
            pages = pageRepository.findAll(pageable);
        }

        return ServiceResult.success(pages.map(page -> {
            Map<String, String> permissionKeys = buildPermissionKeys(page.getPageCode());
            return toResponse(page, permissionKeys);
        }));
    }

    /**
     * POST /api/pages/search
     * Dynamic search for pages with filtering, sorting, and pagination
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_VIEW)")
    public ServiceResult<org.springframework.data.domain.Page<PageResponse>> searchPages(SearchRequest request) {
        // Build JPA Specification from filters
        Specification<Page> spec = SpecBuilder.build(
            request,
            new SetAllowedFields(ALLOWED_PAGE_SEARCH_FIELDS),
            DefaultFieldValueConverter.INSTANCE
        );

        // Build Pageable with validated sort fields
        Pageable pageable = PageableBuilder.from(request, ALLOWED_PAGE_SORT_FIELDS);

        org.springframework.data.domain.Page<Page> pages = (spec != null)
                ? pageRepository.findAll(spec, pageable)
                : pageRepository.findAll(pageable);
        return ServiceResult.success(pages.map(page -> {
            Map<String, String> permissionKeys = buildPermissionKeys(page.getPageCode());
            return toResponse(page, permissionKeys);
        }));
    }

    /**
     * Get all active Pages (for dropdown in Role Access Control)
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_VIEW)")
    public ServiceResult<List<PageResponse>> getActivePages() {
        List<Page> pages = pageRepository.findByActiveOrderByDisplayOrder(true);

        return ServiceResult.success(pages.stream()
                .map(page -> {
                    Map<String, String> permissionKeys = buildPermissionKeys(page.getPageCode());
                    return toResponse(page, permissionKeys);
                })
                .toList());
    }

    /**
     * Soft-deletes a page; inactive pages drop out of getActivePages() and user menus but can be reactivated.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_DELETE)")
    public ServiceResult<PageResponse> deactivatePage(Long id) {
        return setPageActive(id, false);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).PAGE_UPDATE)")
    public ServiceResult<PageResponse> reactivatePage(Long id) {
        return setPageActive(id, true);
    }

    private ServiceResult<PageResponse> setPageActive(Long id, boolean active) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.PAGE_NOT_FOUND, id));

        log.info("{} page ID: {} (code: {})", active ? "Reactivating" : "Deactivating", id, page.getPageCode());

        if (active) {
            page.activate();
        } else {
            page.deactivate();
        }

        Page updated = pageRepository.save(page);

        Map<String, String> permissionKeys = buildPermissionKeys(page.getPageCode());
        return ServiceResult.success(toResponse(updated, permissionKeys), Status.UPDATED);
    }

    // Helper Methods

    /**
     * Creates permission RECORDS only (never assigns them to a role — see RoleAccessService.addPageToRole()).
     *
     * @param suppressTypes permission types to skip (e.g. omit DELETE for a page with no delete action)
     */
    private Map<String, String> createPermissionRecords(Page page, Set<PermissionType> suppressTypes) {
        Map<String, String> permissionKeys = new LinkedHashMap<>();
        String pageCode = page.getPageCode();

        for (PermissionType type : PermissionType.values()) {
            if (suppressTypes.contains(type)) {
                continue;
            }
            String permKey = type.buildPermissionKey(pageCode);
            permissionKeys.put(type.name(), permKey);

            // Check if permission already exists
            Optional<Permission> existing = permissionRepository.findByName(permKey);

            if (existing.isEmpty()) {
                // Create new permission RECORD linked to the page
                Permission newPerm = Permission.builder()
                        .name(permKey)
                        .page(page)                    // Link to Page via FK
                        .permissionType(type)          // Store type for efficient queries
                        .build();
                permissionRepository.save(newPerm);
                log.debug("Created permission record: {} linked to page ID: {}", permKey, page.getId());
            } else {
                // Update existing permission to link to page if not already linked
                Permission existingPerm = existing.get();
                if (existingPerm.getPage() == null) {
                    existingPerm.setPage(page);
                    existingPerm.setPermissionType(type);
                    permissionRepository.save(existingPerm);
                    log.debug("Updated permission record: {} to link to page ID: {}", permKey, page.getId());
                } else {
                    log.debug("Permission record already exists and linked: {}", permKey);
                }
            }
        }

        return permissionKeys;
    }

    /**
     * Builds reference-only permission keys; does not create or modify any database records.
     */
    private Map<String, String> buildPermissionKeys(String pageCode) {
        Map<String, String> keys = new LinkedHashMap<>();
        for (PermissionType type : PermissionType.values()) {
            keys.put(type.name(), type.buildPermissionKey(pageCode));
        }
        return keys;
    }

    /**
     * Convert Page entity to PageResponse DTO
     */
    private PageResponse toResponse(Page page, Map<String, String> permissionKeys) {
        return PageResponse.builder()
                .id(page.getId())
                .pageCode(page.getPageCode())
                .nameAr(page.getNameAr())
                .nameEn(page.getNameEn())
                .route(page.getRoute())
                .icon(page.getIcon())
                .module(page.getModule())
                .parentId(page.getParentId())
                .displayOrder(page.getDisplayOrder())
                .active(page.getActive())
                .description(page.getDescription())
                .permissionKeys(permissionKeys)
                .createdAt(page.getCreatedAt())
                .createdBy(page.getCreatedBy())
                .updatedAt(page.getUpdatedAt())
                .updatedBy(page.getUpdatedBy())
                .build();
    }

    private static final String ROUTE_PATTERN = "^/[a-zA-Z0-9/_-]+$";

    private void validateRouteFormat(String route) {
        if (!route.startsWith("/")) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_ROUTE_FORMAT, route);
        }
        if (!route.matches(ROUTE_PATTERN)) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.INVALID_ROUTE_FORMAT, route);
        }
    }
}
