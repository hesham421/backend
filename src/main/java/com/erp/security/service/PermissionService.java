package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.search.PageableBuilder;
import com.erp.security.dto.PermissionResponse;
import com.erp.security.dto.PermissionSearchRequest;
import com.erp.security.entity.Permission;
import com.erp.security.mapper.PermissionMapper;
import com.erp.security.repository.PermissionRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
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
 * Orchestration for ENTITY-SEC-003 (Permission) — API-SEC-014 read-only listing (SCR-SEC-002 VIEW).
 * Permissions are system-generated (RULE-SEC-011), so this service exposes search only — no
 * create/update/delete. The pageFk/permissionType/moduleFk filters (QR-SEC-0015) are nested/coded,
 * so an explicit Specification is built here (build-create-service A.5.17); paging/sort still use
 * the shared PageableBuilder with an ALLOWED_SORT_FIELDS whitelist. No caching (register empty).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository repository;
    private final PermissionMapper mapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "permissionCode", "permissionType", "createdAt"
    );

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_ROLES_VIEW)")
    public ServiceResult<Page<PermissionResponse>> search(PermissionSearchRequest searchRequest) {
        log.debug("Searching Permission");

        Specification<Permission> spec = buildSpecification(searchRequest);
        Pageable pageable = PageableBuilder.from(searchRequest.toCommonSearchRequest(), ALLOWED_SORT_FIELDS);

        Page<Permission> page = repository.findAll(spec, pageable);

        return ServiceResult.success(page.map(mapper::toResponse));
    }

    /**
     * QR-SEC-0015 — an explicit Specification for the nested/coded filters the generic SpecBuilder
     * cannot express: permissionType (fixed CORE-9 code, EXACT), pageFk ({@code page.id}, EXACT)
     * and moduleFk ({@code page.module.id}, EXACT — via the permission's page). Empty request → no
     * predicates → returns all.
     */
    private Specification<Permission> buildSpecification(PermissionSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getPermissionType() != null && !request.getPermissionType().isBlank()) {
                predicates.add(cb.equal(root.get("permissionType"), request.getPermissionType()));
            }
            if (request.getPageFk() != null) {
                predicates.add(cb.equal(root.get("page").get("id"), request.getPageFk()));
            }
            if (request.getModuleFk() != null) {
                predicates.add(cb.equal(root.get("page").get("module").get("id"), request.getModuleFk()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
