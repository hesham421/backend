package com.erp.masterdata.service;

import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SpecBuilder;
import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.masterdata.dto.*;
import com.erp.masterdata.entity.MdLookupDetail;
import com.erp.masterdata.entity.MdMasterLookup;
import com.erp.masterdata.exception.MasterDataErrorCodes;
import com.erp.masterdata.mapper.LookupDetailMapper;
import com.erp.masterdata.repository.LookupDetailRepository;
import com.erp.masterdata.repository.MasterLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LookupDetailService {

    private final LookupDetailRepository lookupDetailRepository;
    private final MasterLookupRepository masterLookupRepository;
    private final LookupDetailMapper lookupDetailMapper;

    /**
     * Allowed sort fields for search
     * Rule 17.3: Sort field whitelist
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "code", "nameAr", "nameEn", "sortOrder", 
        "isActive", "createdAt", "updatedAt"
    );

    /**
     * Code must be unique within the same master lookup; master lookup must exist.
     */
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "lookupValues", allEntries = true)
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_CREATE)")
    public ServiceResult<LookupDetailResponse> create(LookupDetailCreateRequest request) {
        log.info("Creating lookup detail with code: {} for master lookup ID: {}", 
                 request.getCode(), request.getMasterLookupId());

        // Validate master lookup exists
        MdMasterLookup masterLookup = masterLookupRepository.findById(request.getMasterLookupId())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.MASTER_LOOKUP_NOT_FOUND,
                request.getMasterLookupId()
            ));

        // Validate unique code within master lookup
        if (lookupDetailRepository.existsByMasterLookupIdAndCode(
                request.getMasterLookupId(), request.getCode())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, MasterDataErrorCodes.LOOKUP_DETAIL_CODE_DUPLICATE, request.getCode());
        }

        // Create entity (mapper sets parent FK — compile-time safety)
        MdLookupDetail entity = lookupDetailMapper.toEntity(request, masterLookup);

        // Save
        MdLookupDetail saved = lookupDetailRepository.save(entity);
        
        log.info("Lookup detail created with ID: {}", saved.getId());
        return ServiceResult.success(lookupDetailMapper.toResponse(saved), Status.CREATED);
    }

    /**
     * masterLookupId and code are immutable; only nameAr, nameEn, extraValue, and sortOrder can be
     * updated.
     */
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "lookupValues", allEntries = true)
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_UPDATE)")
    public ServiceResult<LookupDetailResponse> update(Long id, LookupDetailUpdateRequest request) {
        log.info("Updating lookup detail ID: {}", id);

        // Find entity
        MdLookupDetail entity = lookupDetailRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.LOOKUP_DETAIL_NOT_FOUND,
                id
            ));

        // Update entity (masterLookupId and code are NOT updated - immutable per contract)
        lookupDetailMapper.updateEntityFromRequest(entity, request);

        // Save
        MdLookupDetail updated = lookupDetailRepository.save(entity);
        
        log.info("Lookup detail updated: {}", id);
        return ServiceResult.success(lookupDetailMapper.toResponse(updated), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_VIEW)")
    public ServiceResult<LookupDetailResponse> getById(Long id) {
        log.debug("Getting lookup detail by ID: {}", id);

        MdLookupDetail entity = lookupDetailRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.LOOKUP_DETAIL_NOT_FOUND,
                id
            ));

        return ServiceResult.success(lookupDetailMapper.toResponse(entity));
    }

    /**
     * Uses the repository method directly when there are no dynamic filters; otherwise builds a
     * Specification with an explicit JOIN (not implicit path navigation) for masterLookupId plus
     * the dynamic filters.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_VIEW)")
    public ServiceResult<Page<LookupDetailResponse>> search(Long masterLookupId, SearchRequest searchRequest) {
        log.debug("Searching lookup details for masterLookupId: {} with filters: {}", masterLookupId, searchRequest);

        // Validate required parent ID
        if (masterLookupId == null) {
            log.warn("masterLookupId is null - returning empty results");
            return ServiceResult.success(Page.empty());
        }

        // Build pageable with default sort by sortOrder ASC
        if (searchRequest == null) {
            searchRequest = new SearchRequest();
        }
        if (searchRequest.getSortBy() == null) {
            searchRequest.setSortBy("sortOrder");
            searchRequest.setSortDir("ASC");
        }
        Pageable pageable = PageableBuilder.from(searchRequest, ALLOWED_SORT_FIELDS);

        // Check if there are additional dynamic filters
        boolean hasAdditionalFilters = searchRequest.getFilters() != null && !searchRequest.getFilters().isEmpty();
        
        if (!hasAdditionalFilters) {
            // Best Practice: Use repository method with explicit JOIN when no dynamic filters
            Page<MdLookupDetail> page = lookupDetailRepository.searchByMasterLookupId(
                masterLookupId, pageable);
            return ServiceResult.success(page.map(lookupDetailMapper::toResponse));
        }

        // Dynamic filters exist - use Specification with explicit Join
        Specification<MdLookupDetail> spec = buildMasterDetailSpecification(masterLookupId);
        
        // Build additional specification from other filters
        com.erp.common.search.AllowedFields allowedFields = 
            new com.erp.common.search.SetAllowedFields(ALLOWED_SORT_FIELDS);
        com.erp.common.search.FieldValueConverter converter = 
            com.erp.common.search.DefaultFieldValueConverter.INSTANCE;
        
        Specification<MdLookupDetail> additionalSpec = SpecBuilder.build(searchRequest, allowedFields, converter);
        
        if (additionalSpec != null) {
            spec = spec.and(additionalSpec);
        }

        // Execute search
        Page<MdLookupDetail> page = lookupDetailRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(lookupDetailMapper::toResponse));
    }

    private Specification<MdLookupDetail> buildMasterDetailSpecification(Long masterLookupId) {
        return (root, query, cb) -> {
            // Explicit JOIN on masterLookup relationship
            Join<MdLookupDetail, MdMasterLookup> masterLookupJoin = root.join("masterLookup", JoinType.INNER);
            
            return cb.equal(masterLookupJoin.get("id"), masterLookupId);
        };
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_VIEW)")
    public ServiceResult<List<LookupDetailOptionResponse>> getOptionsByLookupKey(String lookupKey, Boolean activeOnly) {
        log.debug("Getting lookup options for key: {}, activeOnly: {}", lookupKey, activeOnly);

        Boolean active = activeOnly != null && activeOnly;

        List<MdLookupDetail> details = lookupDetailRepository.findByMasterLookupKeyAndActive(
            lookupKey.toUpperCase(), active);

        return ServiceResult.success(details.stream()
            .map(lookupDetailMapper::toOptionResponse)
            .collect(Collectors.toList()));
    }

    @org.springframework.cache.annotation.CacheEvict(cacheNames = "lookupValues", allEntries = true)
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_UPDATE)")
    public ServiceResult<LookupDetailResponse> toggleActive(Long id, Boolean active) {
        log.info("Toggling lookup detail ID: {} to active={}", id, active);

        MdLookupDetail entity = lookupDetailRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.LOOKUP_DETAIL_NOT_FOUND,
                id
            ));

        if (Boolean.TRUE.equals(active)) {
            entity.activate();
        } else {
            entity.deactivate();
        }

        MdLookupDetail updated = lookupDetailRepository.save(entity);
        
        log.info("Lookup detail {} toggled to active={}", id, active);
        return ServiceResult.success(lookupDetailMapper.toResponse(updated), Status.UPDATED);
    }

    /**
     * Cannot delete if referenced by any active entity — returns HTTP 409 CONFLICT.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_DELETE)")
    public void delete(Long id) {
        log.info("Deleting lookup detail ID: {}", id);

        MdLookupDetail entity = lookupDetailRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.LOOKUP_DETAIL_NOT_FOUND,
                id
            ));

        // Delete - DataIntegrityViolationException handled by GlobalExceptionHandler
        lookupDetailRepository.delete(entity);
        log.info("Lookup detail deleted: {}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_VIEW)")
    public ServiceResult<LookupDetailUsageResponse> getUsage(Long id) {
        log.debug("Getting usage for lookup detail ID: {}", id);

        MdLookupDetail entity = lookupDetailRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.LOOKUP_DETAIL_NOT_FOUND,
                id
            ));

        return ServiceResult.success(lookupDetailMapper.toUsageResponse(entity));
    }
}
