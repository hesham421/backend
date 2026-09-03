package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.security.domain.RoleDomain;
import com.erp.security.dto.RoleCreateRequest;
import com.erp.security.dto.RoleResponse;
import com.erp.security.dto.RoleSearchRequest;
import com.erp.security.dto.RoleUpdateRequest;
import com.erp.security.entity.Role;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.mapper.RoleMapper;
import com.erp.security.repository.RoleRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for ENTITY-SEC-002 (Role) — API-SEC-011 (Roles CRUD), SCR-SEC-002. Addresses the
 * resource by surrogate id ({@code /roles/{id}}); DELETE is a soft deactivate. No caching
 * annotations — the project's cache-eligibility register is empty (gov-enforce-caching-rules
 * D.1.1/D.5.5). Business rules delegate to RoleDomain (RULE-SEC-010 uniqueness + required fields).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository repository;
    private final RoleMapper mapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "roleCode", "nameAr", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_CREATE)")
    public ServiceResult<RoleResponse> create(RoleCreateRequest request) {
        log.info("Creating Role with code: {}", request.getRoleCode());

        boolean codeTaken = repository.existsByRoleCode(normalize(request.getRoleCode()));

        RoleDomain.create(request.getRoleCode(), request.getNameAr(), request.getNameEn(), codeTaken);

        Role saved = repository.save(mapper.toEntity(request));
        log.info("Created Role ID: {}, code: {}", saved.getId(), saved.getRoleCode());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_VIEW)")
    public ServiceResult<Page<RoleResponse>> search(RoleSearchRequest searchRequest) {
        log.debug("Searching Role");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();

        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<Role> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<Role> page = repository.findAll(spec, pageable);

        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_UPDATE)")
    public ServiceResult<RoleResponse> update(Long id, RoleUpdateRequest request) {
        log.info("Updating Role ID: {}", id);

        Role entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, id));

        // roleCode immutability (RULE-SEC-010) needs no guard — it is absent from the request.
        RoleDomain.from(entity).assertCanUpdate(request.getNameAr(), request.getNameEn());

        mapper.updateEntityFromRequest(entity, request);
        Role saved = repository.save(entity);
        log.info("Updated Role ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_VIEW)")
    public ServiceResult<RoleResponse> getById(Long id) {
        log.debug("Fetching Role ID: {}", id);

        Role entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, id));

        return ServiceResult.success(mapper.toResponse(entity));
    }

    /**
     * API-SEC-011 DELETE — soft deactivate (Role has no hard-delete lifecycle). Mirrors the Module
     * deactivate: find → not-found throw → entity.deactivate() → save. Returns void so the
     * controller responds 204.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_DELETE)")
    public void deactivate(Long id) {
        log.info("Deactivating Role ID: {}", id);

        Role entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.ROLE_NOT_FOUND, id));

        entity.deactivate();
        repository.save(entity);
        log.info("Deactivated Role ID: {}", id);
    }

    /**
     * Normalizes a caller-supplied roleCode to the canonical uppercase form Role.onCreate() always
     * applies before persisting, so the uniqueness check matches the stored value.
     */
    private static String normalize(String roleCode) {
        return roleCode == null ? null : roleCode.trim().toUpperCase();
    }
}
