package com.erp.mdm.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.mdm.domain.LookupValueDomain;
import com.erp.mdm.dto.LookupValueCreateRequest;
import com.erp.mdm.dto.LookupValueLite;
import com.erp.mdm.dto.LookupValueResponse;
import com.erp.mdm.dto.LookupValueSearchRequest;
import com.erp.mdm.dto.LookupValueUpdateRequest;
import com.erp.mdm.entity.LookupType;
import com.erp.mdm.entity.LookupValue;
import com.erp.mdm.exception.MdmErrorCodes;
import com.erp.mdm.mapper.LookupValueMapper;
import com.erp.mdm.repository.LookupTypeRepository;
import com.erp.mdm.repository.LookupValueRepository;
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
 * Orchestration for the MDM LookupValue reads/writes. This sub (SVC-API-CONSUMPTION) creates it
 * with only the platform-provider read {@link #findActiveByTypeCode(String)} (API-MDM-011); the
 * SVC-API-LOOKUP-VALUE sub appends the CRUD/search methods additively.
 *
 * <p>No caching: MDM is absent from the caching approved-register, so per gov-enforce-caching-rules
 * this service carries zero {@code @Cacheable}/{@code @CacheEvict} annotations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MdmLookupValueService {

    private final LookupValueRepository lookupValueRepository;
    private final LookupTypeRepository lookupTypeRepository;
    private final LookupValueMapper mapper;

    /** Entity property names (isActive, NOT the DTO's isActiveFl) — filter + sort whitelist. */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "valueCode", "nameAr", "nameEn", "sortOrder", "isActive"
    );

    /**
     * API-MDM-011 — active values under an active type, resolved by the parent's typeCode. Unknown,
     * typo or inactive typeCode all resolve to an empty list (never a 404) per the spec, so no
     * error is thrown. The lookup key is upper-cased to match the entity's {@code @PrePersist}
     * normalization of typeCode.
     *
     * <p><b>{@code @PreAuthorize("isAuthenticated()")} — deliberate, spec-mandated deviation from
     * build-create-service A.5.2's permission-constant form (DRV-006 / srs-MDM §B5).</b> This is a
     * platform-wide provider endpoint intentionally NOT gated by SCR-MDM-001 permissions: any
     * authenticated caller/service may consume it. SecurityConfig's global
     * {@code .anyRequest().authenticated()} already requires a JWT; the method-level annotation
     * makes that intent explicit.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<List<LookupValueLite>> findActiveByTypeCode(String typeCode) {
        log.debug("Resolving active MDM lookup values for typeCode: {}", typeCode);

        String normalized = MdmSearchSupport.normalizeCode(typeCode);

        return ServiceResult.success(lookupValueRepository.findActiveByTypeCode(normalized));
    }

    /**
     * API-MDM-006 create — load the parent LookupType by {typeId} (existence only, ERR-0007), enforce
     * RULE-MDM-003 valueCode uniqueness within the type + RULE-MDM-005 required names via
     * LookupValueDomain, then persist with the parent FK set by the service (mapper stays scalar).
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_CREATE)")
    public ServiceResult<LookupValueResponse> create(Long typeId, LookupValueCreateRequest request) {
        log.info("Creating LookupValue with code: {} under type ID: {}", request.getValueCode(), typeId);

        // QR-MDM-0011 (DRV-004) — parent existence; existence only, no active-check.
        LookupType parent = lookupTypeRepository.findById(typeId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_TYPE_PARENT_NOT_FOUND, typeId));

        // RULE-MDM-003 pre-check against the normalized (uppercase) code — LookupValue.onCreate()
        // uppercases valueCode before insert, so the uniqueness probe must match the stored form.
        boolean codeTaken = lookupValueRepository.existsByLookupTypeIdAndValueCode(
            typeId, MdmSearchSupport.normalizeCode(request.getValueCode()));

        // Delegate the decision (required names RULE-MDM-005 + RULE-MDM-003 uniqueness) to the Domain
        LookupValueDomain.create(request.getValueCode(), request.getNameAr(), request.getNameEn(), codeTaken);

        LookupValue entity = mapper.toEntity(request);
        entity.setLookupType(parent);

        LookupValue saved = lookupValueRepository.save(entity);
        log.info("Created LookupValue ID: {}, code: {}", saved.getId(), saved.getValueCode());

        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    /**
     * API-MDM-007 list — parent-scoped search. Requires the parent to exist (ERR-0007), defaults to
     * active-only when the caller supplies no isActive filter (DRV-011), then scopes the spec to the
     * parent type via the lookupType association id (mirrors PageService).
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_VIEW)")
    public ServiceResult<Page<LookupValueResponse>> search(Long typeId, LookupValueSearchRequest searchRequest) {
        log.debug("Searching LookupValue under type ID: {}", typeId);

        // QR-MDM-0011 (DRV-004) — parent existence.
        if (!lookupTypeRepository.existsById(typeId)) {
            throw new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_TYPE_PARENT_NOT_FOUND, typeId);
        }

        // DRV-011 — default to active-only when the caller supplies no isActive filter.
        searchRequest.setFilters(MdmSearchSupport.withDefaultActiveFilter(searchRequest.getFilters()));

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();

        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<LookupValue> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        // Scope to the parent type (path {typeId}) — mirrors PageService's parent-scoped search.
        spec = spec.and((root, query, cb) -> cb.equal(root.get("lookupType").get("id"), typeId));

        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<LookupValue> page = lookupValueRepository.findAll(spec, pageable);

        return ServiceResult.success(page.map(mapper::toResponse));
    }

    /**
     * API-MDM-008 update — RULE-MDM-005 required names via LookupValueDomain; valueCode (RULE-MDM-004)
     * and the parent are immutable and structurally absent from the request.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_UPDATE)")
    public ServiceResult<LookupValueResponse> update(Long id, LookupValueUpdateRequest request) {
        log.info("Updating LookupValue ID: {}", id);

        LookupValue entity = lookupValueRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_VALUE_NOT_FOUND, id));

        LookupValueDomain.from(entity).assertCanUpdate(request.getNameAr(), request.getNameEn());

        mapper.updateEntityFromRequest(entity, request);
        LookupValue saved = lookupValueRepository.save(entity);
        log.info("Updated LookupValue ID: {}", saved.getId());

        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_VIEW)")
    public ServiceResult<LookupValueResponse> getById(Long id) {
        log.debug("Fetching LookupValue ID: {}", id);

        LookupValue entity = lookupValueRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_VALUE_NOT_FOUND, id));

        return ServiceResult.success(mapper.toResponse(entity));
    }

    /**
     * API-MDM-009 DELETE — soft deactivate. LookupValue is a leaf (DRV-008) — no child/usage check.
     * find → not-found throw → entity.deactivate() → save. Returns void so the controller responds 204.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.permission.PermissionConstants).PERM_MDM_LOOKUP_DELETE)")
    public void deactivate(Long id) {
        log.info("Deactivating LookupValue ID: {}", id);

        LookupValue entity = lookupValueRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, MdmErrorCodes.LOOKUP_VALUE_NOT_FOUND, id));

        entity.deactivate();
        lookupValueRepository.save(entity);
        log.info("Deactivated LookupValue ID: {}", id);
    }
}
