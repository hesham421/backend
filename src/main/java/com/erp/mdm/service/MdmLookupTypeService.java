package com.erp.mdm.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.mdm.domain.LookupTypeDomain;
import com.erp.mdm.dto.LookupTypeCreateRequest;
import com.erp.mdm.dto.LookupTypeResponse;
import com.erp.mdm.dto.LookupTypeSearchRequest;
import com.erp.mdm.dto.LookupTypeUpdateRequest;
import com.erp.mdm.entity.LookupType;
import com.erp.mdm.exception.MdmErrorCodes;
import com.erp.mdm.mapper.LookupTypeMapper;
import com.erp.mdm.repository.LookupTypeRepository;
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
 * Orchestration for ENTITY-MDM-001 (LookupType) — API-MDM-001..005 (LookupType master CRUD),
 * SCR-MDM-001. Addresses the resource by surrogate id ({@code /lookup-types/{id}}); DELETE is a soft
 * deactivate (204). Business rules delegate to LookupTypeDomain.
 *
 * <p>No caching annotations — MDM is absent from the caching approved-register, so per
 * gov-enforce-caching-rules this service carries zero {@code @Cacheable}/{@code @CacheEvict}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MdmLookupTypeService {

    private final LookupTypeRepository repository;
    private final LookupTypeMapper mapper;

    /** Entity property names (isActive, NOT the DTO's isActiveFl) — filter + sort whitelist. */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "typeCode", "nameAr", "nameEn", "isActive"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_CREATE)")
    public ServiceResult<LookupTypeResponse> create(LookupTypeCreateRequest request) {
        log.info("Creating LookupType with code: {}", request.getTypeCode());

        // RULE-MDM-001 pre-check against the normalized (uppercase) code — LookupType.onCreate()
        // uppercases typeCode before insert, so the uniqueness probe must match the stored form.
        boolean codeTaken = repository.existsByTypeCode(MdmSearchSupport.normalizeCode(request.getTypeCode()));

        // Delegate the decision (required names RULE-MDM-005 + RULE-MDM-001 uniqueness) to the Domain
        LookupTypeDomain.create(request.getTypeCode(), request.getNameAr(), request.getNameEn(), codeTaken);

        LookupType saved = repository.save(mapper.toEntity(request));
        log.info("Created LookupType ID: {}, code: {}", saved.getId(), saved.getTypeCode());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_VIEW)")
    public ServiceResult<Page<LookupTypeResponse>> search(LookupTypeSearchRequest searchRequest) {
        log.debug("Searching LookupType");

        // DRV-011 — default to active-only when the caller supplies no isActive filter.
        searchRequest.setFilters(MdmSearchSupport.withDefaultActiveFilter(searchRequest.getFilters()));

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();

        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<LookupType> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<LookupType> page = repository.findAll(spec, pageable);

        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_UPDATE)")
    public ServiceResult<LookupTypeResponse> update(Long id, LookupTypeUpdateRequest request) {
        log.info("Updating LookupType ID: {}", id);

        LookupType entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_TYPE_NOT_FOUND, id));

        // typeCode immutability (RULE-MDM-002) needs no guard — it is absent from the request.
        LookupTypeDomain.from(entity).assertCanUpdate(request.getNameAr(), request.getNameEn());

        mapper.updateEntityFromRequest(entity, request);
        LookupType saved = repository.save(entity);
        log.info("Updated LookupType ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_VIEW)")
    public ServiceResult<LookupTypeResponse> getById(Long id) {
        log.debug("Fetching LookupType ID: {}", id);

        LookupType entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_TYPE_NOT_FOUND, id));

        return ServiceResult.success(mapper.toResponse(entity));
    }

    /**
     * API-MDM-004 DELETE — soft deactivate. RULE-MDM-006 (DRV-008): blocked while active
     * LookupValues still exist under this type (QR-MDM-0006 count → Domain guard). No cascade to
     * the value rows (DRV-009). Returns void so the controller responds 204.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_DELETE)")
    public void deactivate(Long id) {
        log.info("Deactivating LookupType ID: {}", id);

        LookupType entity = repository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_TYPE_NOT_FOUND, id));

        long activeValues = repository.countActiveValuesByType(id);
        LookupTypeDomain.from(entity).assertCanDeactivate(activeValues);

        entity.deactivate();
        repository.save(entity);
        log.info("Deactivated LookupType ID: {}", id);
    }
}
