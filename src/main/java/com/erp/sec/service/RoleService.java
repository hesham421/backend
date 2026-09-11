package com.erp.sec.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.search.BooleanFieldValueConverter;
import com.erp.common.search.FieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.sec.domain.RoleDomain;
import com.erp.sec.dto.RoleCreateRequest;
import com.erp.sec.dto.RoleResponse;
import com.erp.sec.dto.RoleSearchRequest;
import com.erp.sec.entity.Role;
import com.erp.sec.mapper.RoleMapper;
import com.erp.sec.repository.RoleRepository;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for ENT-SEC-002 (Role) — API-SEC-012/013. No caching: SEC is absent from the
 * gov-enforce-caching-rules approved register.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    /** SCR-REQ-SEC-005 B2 filters (role code/name, active flag) as entity columns (A.5.6). */
    private static final Set<String> ALLOWED_SORT_FIELDS =
        Set.of("code", "nameAr", "nameEn", "isActiveFl");

    /** {@code isActiveFl} arrives from a JSON body, which may carry it as a string. */
    private static final FieldValueConverter FILTER_VALUE_CONVERTER =
        new BooleanFieldValueConverter(Set.of("isActiveFl"));

    private final RoleRepository repository;
    private final RoleMapper mapper;

    /** API-SEC-013 — the uniqueness probe matches the stored form, which {@code @PrePersist} uppercases. */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_ROLES_CREATE)")
    public ServiceResult<RoleResponse> create(RoleCreateRequest request) {
        log.info("Creating Role with code: {}", request.getCode());

        boolean codeTaken = repository.existsByCode(normalize(request.getCode()));

        RoleDomain.create(request.getCode(), codeTaken);

        Role saved = repository.save(mapper.toEntity(request));
        log.info("Created Role ID: {}, code: {}", saved.getRolePk(), saved.getCode());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    /** API-SEC-012 — an empty match is success with empty content, never a 404. */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_SEC_ROLES_VIEW)")
    public ServiceResult<Page<RoleResponse>> search(RoleSearchRequest searchRequest) {
        log.debug("Searching Role");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SecSearchSupport.assertSortAllowed(commonRequest.getSortField(), ALLOWED_SORT_FIELDS);

        Specification<Role> spec = SpecBuilder.build(commonRequest,
            new SetAllowedFields(ALLOWED_SORT_FIELDS), FILTER_VALUE_CONVERTER);
        String name = searchRequest.getName();
        if (name != null && !name.isBlank()) {
            spec = spec.and(nameMatches(name));
        }

        Page<Role> result =
            repository.findAll(spec, PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS));

        return ServiceResult.success(result.map(mapper::toResponse));
    }

    /** {@code name} matches either language column (API-SEC-012 Request line) — no shared OR operator. */
    private Specification<Role> nameMatches(String name) {
        String pattern = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get("nameAr")), pattern),
            cb.like(cb.lower(root.get("nameEn")), pattern));
    }

    private String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }
}
