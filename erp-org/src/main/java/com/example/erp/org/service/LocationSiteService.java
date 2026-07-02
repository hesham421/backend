package com.example.erp.org.service;

import com.erp.common.search.AllowedFields;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.domain.OrgLocationSiteDomain;
import com.example.erp.org.dto.LocationSiteCreateRequest;
import com.example.erp.org.dto.LocationSiteResponse;
import com.example.erp.org.dto.LocationSiteSearchRequest;
import com.example.erp.org.dto.LocationSiteUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgLocationSite;
import com.example.erp.org.exception.OrgErrorCodes;
import com.example.erp.org.mapper.LocationSiteMapper;
import com.example.erp.org.repository.BranchRepository;
import com.example.erp.org.repository.LocationSiteRepository;
import com.example.erp.org.service.support.OrgNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Orchestration for LocationSite (API-ORG-039..044). Business Rule decisions (RULE-ORG-019
 * parent-active create guard) are delegated to {@link OrgLocationSiteDomain}. No entity-specific
 * deactivation guard exists here — RULE-ORG-005 is the Branch-side guard, already enforced by
 * {@link com.example.erp.org.domain.OrgBranchDomain} in {@link BranchService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationSiteService {

    private final LocationSiteRepository locationSiteRepository;
    private final BranchRepository branchRepository;
    private final LocationSiteMapper locationSiteMapper;
    private final OrgNumberGenerator orgNumberGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "locationSiteCode", "nameAr", "nameEn", "branch.id", "siteTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LOCATION_SITE_CREATE)")
    public ServiceResult<LocationSiteResponse> create(LocationSiteCreateRequest request) {
        log.info("Creating LocationSite under Branch ID: {}", request.getBranchFk());

        OrgBranch parent = branchRepository.findById(request.getBranchFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "Branch", request.getBranchFk()));

        assertNameNotDuplicate(parent.getId(), request.getNameAr(), request.getNameEn(), null);

        String generatedCode = orgNumberGenerator.next(
            "LS-" + parent.getBranchCode() + "-",
            locationSiteRepository.countByBranch_Id(parent.getId()),
            code -> locationSiteRepository.existsByBranch_IdAndLocationSiteCode(parent.getId(), code));
        OrgLocationSiteDomain.create(generatedCode, Boolean.TRUE.equals(parent.getIsActiveFl()));

        OrgLocationSite entity = locationSiteMapper.toEntity(request, generatedCode, parent);
        OrgLocationSite saved = locationSiteRepository.save(entity);

        log.info("Created LocationSite ID: {}, code: {}", saved.getId(), saved.getLocationSiteCode());
        return ServiceResult.success(locationSiteMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LOCATION_SITE_UPDATE)")
    public ServiceResult<LocationSiteResponse> update(Long id, LocationSiteUpdateRequest request) {
        log.info("Updating LocationSite ID: {}", id);

        OrgLocationSite entity = findOrThrow(id);
        assertNameNotDuplicate(entity.getBranch().getId(), request.getNameAr(), request.getNameEn(), id);

        locationSiteMapper.updateEntityFromRequest(entity, request);
        OrgLocationSite saved = locationSiteRepository.save(entity);

        log.info("Updated LocationSite ID: {}", saved.getId());
        return ServiceResult.success(locationSiteMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LOCATION_SITE_VIEW)")
    public ServiceResult<LocationSiteResponse> getById(Long id) {
        log.debug("Fetching LocationSite ID: {}", id);
        return ServiceResult.success(locationSiteMapper.toResponse(findOrThrow(id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LOCATION_SITE_VIEW)")
    public ServiceResult<Page<LocationSiteResponse>> search(LocationSiteSearchRequest searchRequest) {
        log.debug("Searching LocationSite");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        AllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgLocationSite> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<OrgLocationSite> page = locationSiteRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(locationSiteMapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LOCATION_SITE_UPDATE)")
    public ServiceResult<LocationSiteResponse> activate(Long id) {
        log.info("Activating LocationSite ID: {}", id);

        OrgLocationSite entity = findOrThrow(id);
        entity.activate();
        OrgLocationSite saved = locationSiteRepository.save(entity);

        log.info("Activated LocationSite ID: {}", saved.getId());
        return ServiceResult.success(locationSiteMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LOCATION_SITE_UPDATE)")
    public ServiceResult<LocationSiteResponse> deactivate(Long id) {
        log.info("Deactivating LocationSite ID: {}", id);

        // No entity-specific deactivation guard for LocationSite (RULE-ORG-005 is the Branch-side
        // guard, already enforced by OrgBranchDomain in BranchService).
        OrgLocationSite entity = findOrThrow(id);
        entity.deactivate();
        OrgLocationSite saved = locationSiteRepository.save(entity);

        log.info("Deactivated LocationSite ID: {}", saved.getId());
        return ServiceResult.success(locationSiteMapper.toResponse(saved), Status.UPDATED);
    }

    private OrgLocationSite findOrThrow(Long id) {
        return locationSiteRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "LocationSite", id));
    }

    /** RULE-ORG-015 — name uniqueness within the parent Branch scope. {@code excludeId} null on create. */
    private void assertNameNotDuplicate(Long branchId, String nameAr, String nameEn, Long excludeId) {
        if (nameAr != null) {
            boolean taken = excludeId == null
                ? locationSiteRepository.existsByBranch_IdAndNameAr(branchId, nameAr)
                : locationSiteRepository.existsByBranch_IdAndNameArAndIdNot(branchId, nameAr, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameAr);
            }
        }
        if (nameEn != null) {
            boolean taken = excludeId == null
                ? locationSiteRepository.existsByBranch_IdAndNameEn(branchId, nameEn)
                : locationSiteRepository.existsByBranch_IdAndNameEnAndIdNot(branchId, nameEn, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameEn);
            }
        }
    }
}
