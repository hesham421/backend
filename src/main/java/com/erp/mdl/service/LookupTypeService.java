package com.erp.mdl.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.BooleanFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchOperator;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.mdl.domain.LookupTypeDomain;
import com.erp.mdl.dto.LookupTypeCreateRequest;
import com.erp.mdl.dto.LookupTypeResponse;
import com.erp.mdl.dto.LookupTypeUpdateRequest;
import com.erp.mdl.dto.OwnerGroupResponse;
import com.erp.mdl.entity.LookupType;
import com.erp.mdl.exception.MdlErrorCodes;
import com.erp.mdl.mapper.LookupTypeMapper;
import com.erp.mdl.repository.LookupTypeRepository;
import com.erp.sec.crossmodule.SecModuleRegistryApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration layer for ENT-MDL-001 (LookupType) — API-MDL-002/003/004 (SVC-API-CRUD.md) plus
 * API-MDL-001/010 (SVC-API-SEARCH.md, read-only). {@code ALLOWED_SORT_FIELDS} is declared now
 * that {@link #search} exists (A.5.6).
 *
 * <p>{@link SecModuleRegistryApi} is the ONLY cross-module interface reference in this module,
 * held here and nowhere else (never in {@code LookupTypeDomain}, the mapper, or the controller) —
 * build-create-service's "Cross-Module Calls" rule.
 *
 * <p>No caching annotations anywhere in this class: MDL's entities are absent from the project's
 * cache-eligibility register (currently empty), so neither {@code @Cacheable} nor
 * {@code @CacheEvict} applies (gov-enforce-caching-rules D.1.1/D.5.5) — and {@link #search} /
 * {@link #browseByOwner} are paginated/listing reads regardless, which the caching skill forbids
 * caching on independently of the register.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LookupTypeService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "lookupTypePk", "key", "ownerModuleCode", "nameAr", "nameEn", "isActiveFl", "createdAt"
    );

    private final LookupTypeRepository repository;
    private final LookupTypeMapper mapper;
    private final SecModuleRegistryApi secModuleRegistryApi;

    /**
     * API-MDL-002 — RULE-MDL-001 (QR-MDL-012, XM-MDL-001) then key uniqueness (QR-MDL-013), both
     * resolved here and delegated to {@link LookupTypeDomain#create} for the actual decision.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_MDL_LOOKUPS_CREATE)")
    public ServiceResult<LookupTypeResponse> create(LookupTypeCreateRequest request) {
        log.info("Creating LookupType with key: {}", request.getKey());

        // 1. XM-MDL-001 / QR-MDL-012 — cross-module SOFT-READ, resolved by the service
        boolean ownerModuleRegistered = secModuleRegistryApi.isModuleActive(request.getOwnerModuleCode());

        // 2. QR-MDL-013 — key-uniqueness pre-check
        boolean keyAlreadyTaken = repository.existsByKey(request.getKey());

        // 3. Delegate the decision — throws LocalizedException on violation
        LookupTypeDomain.create(request.getKey(), request.getOwnerModuleCode(),
            ownerModuleRegistered, keyAlreadyTaken);

        // 4. Map, then persist (QR-MDL-002)
        LookupType saved = repository.save(mapper.toEntity(request));
        log.info("Created LookupType ID: {}", saved.getLookupTypePk());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    /**
     * API-MDL-003 — name-only update. RULE-MDL-003 (key immutability) needs no runtime guard
     * here: {@code key} is structurally absent from {@link LookupTypeUpdateRequest}.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_MDL_LOOKUPS_UPDATE)")
    public ServiceResult<LookupTypeResponse> update(Long id, LookupTypeUpdateRequest request) {
        log.info("Updating LookupType ID: {}", id);

        LookupType entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, MdlErrorCodes.MDL_404_TYPE, id));

        mapper.updateEntityFromRequest(entity, request);
        LookupType saved = repository.save(entity);
        log.info("Updated LookupType ID: {}", saved.getLookupTypePk());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    /**
     * API-MDL-004 — soft-deactivate only, no active-children guard (DATA-DOM's own report and this
     * API's orchestration line both confirm RULE-MDL-004 is a read-time filter owned by the future
     * consumer read, API-MDL-011 — not a write-time guard here).
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_MDL_LOOKUPS_UPDATE)")
    public ServiceResult<LookupTypeResponse> deactivate(Long id) {
        log.info("Deactivating LookupType ID: {}", id);

        LookupType entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, MdlErrorCodes.MDL_404_TYPE, id));

        entity.deactivate();
        LookupType saved = repository.save(entity);
        log.info("Deactivated LookupType ID: {}", saved.getLookupTypePk());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    /**
     * API-MDL-001 — search lookup types (SVC-API-SEARCH.md). {@code key} is LIKE, {@code
     * ownerModuleCode}/{@code isActiveFl} are EXACT. Design note (SVC-API-SEARCH.md point 7):
     * this endpoint is exposed as {@code GET} + query params, not the generic {@code POST
     * /search} + body — the controller passes plain scalar arguments here rather than a
     * {@code SearchRequest}-shaped body DTO; this method builds the internal
     * {@link SearchRequest}/{@link SearchFilter} list itself before handing off to the shared
     * {@link SpecBuilder}/{@link PageableBuilder}, so the shared search plumbing is still reused
     * exactly as any {@code POST /search} method would use it.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_MDL_LOOKUPS_VIEW)")
    public ServiceResult<Page<LookupTypeResponse>> search(String key, String ownerModuleCode,
                                                            Boolean isActiveFl, int page, int size, String sort) {
        log.debug("Searching LookupType");

        List<SearchFilter> filters = new ArrayList<>();
        if (key != null && !key.isBlank()) {
            filters.add(SearchFilter.builder().field("key").operator(SearchOperator.LIKE).value(key).build());
        }
        if (ownerModuleCode != null && !ownerModuleCode.isBlank()) {
            filters.add(SearchFilter.builder()
                .field("ownerModuleCode").operator(SearchOperator.EQUALS).value(ownerModuleCode).build());
        }
        if (isActiveFl != null) {
            filters.add(SearchFilter.builder()
                .field("isActiveFl").operator(SearchOperator.EQUALS).value(isActiveFl).build());
        }

        SearchRequest commonRequest = SearchRequest.builder()
            .filters(filters)
            .sortField(sort)
            .page(page)
            .size(size)
            .build();

        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<LookupType> spec = SpecBuilder.build(
            commonRequest, allowedFields, new BooleanFieldValueConverter(Set.of("isActiveFl")));
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<LookupType> resultPage = repository.findAll(spec, pageable);

        return ServiceResult.success(resultPage.map(mapper::toResponse));
    }

    /**
     * API-MDL-010 — browse the registry grouped by owner (SVC-API-SEARCH.md, QR-MDL-010).
     * Active types only, optionally filtered by {@code ownerModuleCode}(EXACT)/{@code key}(LIKE),
     * then grouped by {@code ownerModuleCode} in the service layer (a plain
     * {@code Collectors.groupingBy} over a single-table load — no SQL {@code GROUP BY}, per
     * DATA-DOM.md's own "grouped in the service layer" wording). Not paginated — the spec's
     * Response line is a flat {@code List<OwnerGroupResponse>}, not a {@code Page<T>}.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.sec.permission.PermissionConstants).PERM_MDL_TYPE_REGISTRY_VIEW)")
    public ServiceResult<List<OwnerGroupResponse>> browseByOwner(String ownerModuleCode, String key) {
        log.debug("Browsing LookupType registry by owner");

        List<SearchFilter> filters = new ArrayList<>();
        filters.add(SearchFilter.builder()
            .field("isActiveFl").operator(SearchOperator.EQUALS).value(Boolean.TRUE).build());
        if (ownerModuleCode != null && !ownerModuleCode.isBlank()) {
            filters.add(SearchFilter.builder()
                .field("ownerModuleCode").operator(SearchOperator.EQUALS).value(ownerModuleCode).build());
        }
        if (key != null && !key.isBlank()) {
            filters.add(SearchFilter.builder().field("key").operator(SearchOperator.LIKE).value(key).build());
        }

        SearchRequest commonRequest = SearchRequest.builder().filters(filters).build();
        SetAllowedFields allowedFields = new SetAllowedFields(Set.of("isActiveFl", "ownerModuleCode", "key"));
        Specification<LookupType> spec = SpecBuilder.build(
            commonRequest, allowedFields, new BooleanFieldValueConverter(Set.of("isActiveFl")));

        List<LookupType> types = repository.findAll(spec);

        Map<String, List<LookupTypeResponse>> grouped = types.stream()
            .map(mapper::toResponse)
            .collect(Collectors.groupingBy(
                LookupTypeResponse::getOwnerModuleCode, TreeMap::new, Collectors.toList()));

        List<OwnerGroupResponse> result = grouped.entrySet().stream()
            .map(entry -> OwnerGroupResponse.builder()
                .ownerModuleCode(entry.getKey())
                .types(entry.getValue())
                .build())
            .toList();

        return ServiceResult.success(result);
    }
}
