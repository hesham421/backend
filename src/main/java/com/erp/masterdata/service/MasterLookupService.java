package com.erp.masterdata.service;

import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SpecBuilder;
import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.masterdata.dto.*;
import com.erp.masterdata.entity.MdMasterLookup;
import com.erp.masterdata.exception.MasterDataErrorCodes;
import com.erp.masterdata.mapper.MasterLookupMapper;
import com.erp.masterdata.repository.MasterLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterLookupService {

    private final MasterLookupRepository masterLookupRepository;
    private final MasterLookupMapper masterLookupMapper;

    /**
     * Allowed sort fields for search
     * Rule 17.3: Sort field whitelist
     */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "lookupKey", "lookupName", "lookupNameEn", 
        "isActive", "createdAt", "updatedAt"
    );

    /**
     * Lookup key must be unique; it is converted to uppercase.
     */
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "lookupValues", allEntries = true)
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_CREATE)")
    public ServiceResult<MasterLookupResponse> create(MasterLookupCreateRequest request) {
        log.info("Creating master lookup with key: {}", request.getLookupKey());

        // Validate unique lookup key (entity @PrePersist normalises to uppercase)
        if (masterLookupRepository.existsByLookupKey(request.getLookupKey().toUpperCase())) {
            throw new LocalizedException(
                Status.ALREADY_EXISTS,
                MasterDataErrorCodes.MASTER_LOOKUP_KEY_DUPLICATE,
                request.getLookupKey()
            );
        }

        // Create entity
        MdMasterLookup entity = masterLookupMapper.toEntity(request);

        // Save
        MdMasterLookup saved = masterLookupRepository.save(entity);
        
        log.info("Master lookup created with ID: {}", saved.getId());
        return ServiceResult.success(masterLookupMapper.toResponse(saved), Status.CREATED);
    }

    /**
     * lookupKey is immutable; only lookupName, lookupNameEn, and description can be updated.
     */
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "lookupValues", allEntries = true)
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_UPDATE)")
    public ServiceResult<MasterLookupResponse> update(Long id, MasterLookupUpdateRequest request) {
        log.info("Updating master lookup ID: {}", id);

        // Find entity
        MdMasterLookup entity = masterLookupRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.MASTER_LOOKUP_NOT_FOUND,
                id
            ));

        // Update entity (lookupKey is NOT updated - immutable per contract)
        masterLookupMapper.updateEntityFromRequest(entity, request);

        // saveAndFlush forces @PreUpdate (AuditEntityListener) to fire before toResponse()
        MdMasterLookup updated = masterLookupRepository.saveAndFlush(entity);
        
        log.info("Master lookup updated: {}", id);
        return ServiceResult.success(masterLookupMapper.toResponse(updated), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_VIEW)")
    public ServiceResult<MasterLookupResponse> getById(Long id) {
        log.debug("Getting master lookup by ID: {}", id);

        MdMasterLookup entity = masterLookupRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.MASTER_LOOKUP_NOT_FOUND,
                id
            ));

        return ServiceResult.success(masterLookupMapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_VIEW)")
    public ServiceResult<Page<MasterLookupResponse>> search(SearchRequest searchRequest) {
        log.debug("Searching master lookups with filters: {}", searchRequest);

        // Build specification using AllowedFields and FieldValueConverter
        com.erp.common.search.AllowedFields allowedFields = 
            new com.erp.common.search.SetAllowedFields(ALLOWED_SORT_FIELDS);
        com.erp.common.search.FieldValueConverter converter = 
            com.erp.common.search.DefaultFieldValueConverter.INSTANCE;
        
        Specification<MdMasterLookup> spec = SpecBuilder.build(searchRequest, allowedFields, converter);

        // Build pageable
        Pageable pageable = PageableBuilder.from(searchRequest, ALLOWED_SORT_FIELDS);

        // Execute search
        Page<MdMasterLookup> page = masterLookupRepository.findAll(spec, pageable);

        return ServiceResult.success(page.map(masterLookupMapper::toResponse));
    }

    /**
     * Cannot deactivate if there are active lookup details.
     */
    @org.springframework.cache.annotation.CacheEvict(cacheNames = "lookupValues", allEntries = true)
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_UPDATE)")
    public ServiceResult<MasterLookupResponse> toggleActive(Long id, Boolean active) {
        log.info("Toggling master lookup ID: {} to active={}", id, active);

        MdMasterLookup entity = masterLookupRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.MASTER_LOOKUP_NOT_FOUND,
                id
            ));

        // Business rule: Cannot deactivate if there are active lookup details
        if (Boolean.FALSE.equals(active)) {
            long activeDetailsCount = masterLookupRepository.countActiveLookupDetails(id);
            if (activeDetailsCount > 0) {
                throw new LocalizedException(
                    Status.CONFLICT,
                    MasterDataErrorCodes.MASTER_LOOKUP_ACTIVE_DETAILS_EXIST,
                    activeDetailsCount
                );
            }
            entity.deactivate();
        } else {
            entity.activate();
        }

        MdMasterLookup updated = masterLookupRepository.save(entity);
        
        log.info("Master lookup {} toggled to active={}", id, active);
        return ServiceResult.success(masterLookupMapper.toResponse(updated), Status.UPDATED);
    }

    /**
     * Cannot delete if it has any lookup details — returns HTTP 409 CONFLICT.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_DELETE)")
    public void delete(Long id) {
        log.info("Deleting master lookup ID: {}", id);

        MdMasterLookup entity = masterLookupRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.MASTER_LOOKUP_NOT_FOUND,
                id
            ));

        // Check for lookup details
        long detailsCount = masterLookupRepository.countLookupDetails(id);
        if (detailsCount > 0) {
            throw new LocalizedException(
                Status.CONFLICT,
                MasterDataErrorCodes.MASTER_LOOKUP_DETAILS_EXIST,
                detailsCount
            );
        }

        // Delete - DataIntegrityViolationException handled by GlobalExceptionHandler
        masterLookupRepository.delete(entity);
        log.info("Master lookup deleted: {}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).MASTER_LOOKUP_VIEW)")
    public ServiceResult<MasterLookupUsageResponse> getUsage(Long id) {
        log.debug("Getting usage for master lookup ID: {}", id);

        MdMasterLookup entity = masterLookupRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                MasterDataErrorCodes.MASTER_LOOKUP_NOT_FOUND,
                id
            ));

        long totalDetailsCount = masterLookupRepository.countLookupDetails(id);
        long activeDetailsCount = masterLookupRepository.countActiveLookupDetails(id);

        return ServiceResult.success(masterLookupMapper.toUsageResponse(entity, totalDetailsCount, activeDetailsCount));
    }
}
