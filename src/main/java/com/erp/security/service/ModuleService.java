package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.security.domain.ModuleDomain;
import com.erp.security.dto.ModuleCreateRequest;
import com.erp.security.dto.ModuleResponse;
import com.erp.security.dto.ModuleSearchRequest;
import com.erp.security.dto.ModuleUpdateRequest;
import com.erp.security.entity.Module;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.mapper.ModuleMapper;
import com.erp.security.repository.ModuleRepository;
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
 * Orchestration for ENTITY-SEC-010 (Module) — API-SEC-020 (Module Registry CRUD), SCR-SEC-004.
 * Addresses the resource by surrogate id ({@code /modules/{id}}); DELETE is a soft deactivate.
 * No caching annotations — the project's cache-eligibility register is empty, so Module is not
 * cache-eligible (gov-enforce-caching-rules D.1.1/D.5.5). Business rules delegate to ModuleDomain.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleService {

    private final ModuleRepository repository;
    private final ModuleMapper mapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "moduleCode", "nameAr", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_MODULES_CREATE)")
    public ServiceResult<ModuleResponse> create(ModuleCreateRequest request) {
        log.info("Creating Module with code: {}", request.getModuleCode());

        // 1. Fetch what RULE-SEC-010 needs — pre-check against the normalized (uppercase) code,
        // since Module.onCreate() uppercases moduleCode before insert.
        boolean codeTaken = repository.existsByModuleCode(normalize(request.getModuleCode()));

        // 2. Delegate the decision (required fields + RULE-SEC-010 uniqueness) to ModuleDomain
        ModuleDomain.create(request.getModuleCode(), request.getNameAr(), request.getNameEn(), codeTaken);

        // 3. Map + persist (SEQ_SEC_MODULE; audit via AuditEntityListener)
        Module saved = repository.save(mapper.toEntity(request));
        log.info("Created Module ID: {}, code: {}", saved.getId(), saved.getModuleCode());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_MODULES_VIEW)")
    public ServiceResult<Page<ModuleResponse>> search(ModuleSearchRequest searchRequest) {
        log.debug("Searching Module");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();

        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<Module> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<Module> page = repository.findAll(spec, pageable);

        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_MODULES_UPDATE)")
    public ServiceResult<ModuleResponse> update(Long id, ModuleUpdateRequest request) {
        log.info("Updating Module ID: {}", id);

        Module entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, id));

        // moduleCode immutability (RULE-SEC-010) needs no guard — it is absent from the request.
        ModuleDomain.from(entity).assertCanUpdate(request.getNameAr(), request.getNameEn());

        mapper.updateEntityFromRequest(entity, request);
        Module saved = repository.save(entity);
        log.info("Updated Module ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_MODULES_VIEW)")
    public ServiceResult<ModuleResponse> getById(Long id) {
        log.debug("Fetching Module ID: {}", id);

        Module entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, id));

        return ServiceResult.success(mapper.toResponse(entity));
    }

    /**
     * API-SEC-020 DELETE — soft deactivate (Module has no hard-delete lifecycle). Mirrors CU's
     * deactivate: find → not-found throw → entity.deactivate() → save. Returns void so the
     * controller responds 204. No reference/child check here: revoke-side dependents
     * (RULE-SEC-014) are enforced on the Tier-1 grant path, not on module deactivation.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_MODULES_DELETE)")
    public void deactivate(Long id) {
        log.info("Deactivating Module ID: {}", id);

        Module entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, id));

        entity.deactivate();
        repository.save(entity);
        log.info("Deactivated Module ID: {}", id);
    }

    /**
     * Normalizes a caller-supplied moduleCode to the canonical uppercase form Module.onCreate()
     * always applies before persisting, so the uniqueness check matches the stored value.
     */
    private static String normalize(String moduleCode) {
        return moduleCode == null ? null : moduleCode.trim().toUpperCase();
    }
}
