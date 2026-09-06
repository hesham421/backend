package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.security.domain.PageDomain;
import com.erp.security.domain.PermissionGenerationDomainService;
import com.erp.security.dto.PageCreateRequest;
import com.erp.security.dto.PageResponse;
import com.erp.security.dto.PageSearchRequest;
import com.erp.security.dto.PageUpdateRequest;
import com.erp.security.entity.Module;
import com.erp.security.entity.Page;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.mapper.PageMapper;
import com.erp.security.repository.ModuleRepository;
import com.erp.security.repository.PageRepository;
import com.erp.security.repository.PermissionRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for ENTITY-SEC-004 (Page, CORE-9 screen registry) — API-SEC-013, SCR-SEC-003.
 * create() is the CORE-9 owner path: validate the owning module, persist the page (QR-SEC-0013),
 * then auto-generate the four screen permissions via PermissionGenerationDomainService
 * (RULE-SEC-011, QR-SEC-0016). No caching (register empty). Business rules delegate to PageDomain.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PageService {

    private final PageRepository repository;
    private final PageMapper mapper;
    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "pageCode", "nameAr", "createdAt"
    );

    /**
     * API-SEC-013 create — validate moduleFk (exists + active → else MODULE_NOT_FOUND) and the
     * optional parentPageFk (→ else PAGE_NOT_FOUND), enforce RULE-SEC-010 pageCode uniqueness via
     * PageDomain, persist the page, then generate its four permissions (RULE-SEC-011). All in one
     * transaction so a generation collision rolls the page back.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_PAGE_REGISTRY_CREATE)")
    public ServiceResult<PageResponse> create(PageCreateRequest request) {
        log.info("Creating Page with code: {}", request.getPageCode());

        Module module = requireActiveModule(request.getModuleFk());
        Page parentPage = resolveParentPage(request.getParentPageFk());

        boolean codeTaken = repository.existsByPageCode(normalize(request.getPageCode()));
        PageDomain.create(request.getPageCode(), request.getNameAr(), request.getNameEn(),
            request.getModuleFk(), codeTaken);

        Page entity = mapper.toEntity(request);
        entity.setModule(module);
        entity.setParentPage(parentPage);

        Page saved = repository.save(entity);
        log.info("Created Page ID: {}, code: {}", saved.getId(), saved.getPageCode());

        // RULE-SEC-011 — auto-generate the 4 screen permissions (PERM_<PAGE_CODE>_<TYPE>).
        Set<String> existingCodes = permissionRepository.findPermissionCodesByPageId(saved.getId());
        permissionRepository.saveAll(
            PermissionGenerationDomainService.generateForPage(saved, existingCodes));
        log.info("Generated screen permissions for Page ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_PAGE_REGISTRY_VIEW)")
    public ServiceResult<org.springframework.data.domain.Page<PageResponse>> search(
            PageSearchRequest searchRequest) {
        log.debug("Searching Page");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();

        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<Page> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);

        Long moduleId = searchRequest.getModuleId();
        if (moduleId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("module").get("id"), moduleId));
        }

        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        org.springframework.data.domain.Page<Page> page = repository.findAll(spec, pageable);

        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_PAGE_REGISTRY_UPDATE)")
    public ServiceResult<PageResponse> update(Long id, PageUpdateRequest request) {
        log.info("Updating Page ID: {}", id);

        Page entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.PAGE_NOT_FOUND, id));

        // pageCode, the owning module and the parent page are all immutable (RULE-SEC-010 / RULE-SEC-014
        // derivation stability) — absent from the request, so an update can only change the display
        // names and the active flag. The module/parent are left exactly as persisted.
        PageDomain.from(entity).assertCanUpdate(request.getNameAr(), request.getNameEn());

        mapper.updateEntityFromRequest(entity, request);

        Page saved = repository.save(entity);
        log.info("Updated Page ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_PAGE_REGISTRY_VIEW)")
    public ServiceResult<PageResponse> getById(Long id) {
        log.debug("Fetching Page ID: {}", id);

        Page entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.PAGE_NOT_FOUND, id));

        return ServiceResult.success(mapper.toResponse(entity));
    }

    /**
     * API-SEC-013 DELETE — soft deactivate (Page has no hard-delete lifecycle). find → not-found
     * throw → entity.deactivate() → save. Returns void so the controller responds 204.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_SEC_PAGE_REGISTRY_DELETE)")
    public void deactivate(Long id) {
        log.info("Deactivating Page ID: {}", id);

        Page entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.PAGE_NOT_FOUND, id));

        entity.deactivate();
        repository.save(entity);
        log.info("Deactivated Page ID: {}", id);
    }

    /** Existence + active guard for the owning module. Missing or inactive → ERR-0012 NOT_FOUND. */
    private Module requireActiveModule(Long moduleFk) {
        Module module = moduleRepository.findById(moduleFk)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, moduleFk));
        if (!Boolean.TRUE.equals(module.getIsActive())) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.MODULE_NOT_FOUND, moduleFk);
        }
        return module;
    }

    /** Resolves the optional parent page. null → no parent; a missing id → ERR-0012 NOT_FOUND. */
    private Page resolveParentPage(Long parentPageFk) {
        if (parentPageFk == null) {
            return null;
        }
        return repository.findById(parentPageFk)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, SecErrorCodes.PAGE_NOT_FOUND, parentPageFk));
    }

    /**
     * Normalizes a caller-supplied pageCode to the canonical uppercase form Page.onCreate() always
     * applies before persisting, so the uniqueness check matches the stored value.
     */
    private static String normalize(String pageCode) {
        return pageCode == null ? null : pageCode.trim().toUpperCase();
    }
}
