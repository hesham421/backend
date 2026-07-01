package com.example.org.service;

import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.org.domain.OrgBranch;
import com.example.org.domain.OrgLocationSite;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgLocationSiteMapper;
import com.example.org.numbering.OrgCodeGenerator;
import com.example.org.repository.OrgBranchRepository;
import com.example.org.repository.OrgLocationSiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgLocationSiteService {

    private final OrgLocationSiteRepository repository;
    private final OrgBranchRepository branchRepository;
    private final OrgLocationSiteMapper mapper;
    private final OrgCodeGenerator codeGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "locationSiteCode", "nameAr", "nameEn", "siteTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_CREATE')")
    public ServiceResult<LocationSiteResponse> create(LocationSiteCreateRequest request) {
        log.info("Creating LocationSite nameEn={} under branchId={}", request.getNameEn(), request.getBranchId());

        OrgBranch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getBranchId()));
        if (!Boolean.TRUE.equals(branch.getIsActiveFl())) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.INACTIVE_BRANCH, request.getBranchId());
        }

        if (repository.existsByNameArIgnoreCaseAndBranchId(request.getNameAr(), request.getBranchId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndBranchId(request.getNameEn(), request.getBranchId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        OrgLocationSite entity = mapper.toEntity(request, branch);
        entity.setLocationSiteCode(codeGenerator.generateLocationSiteCode(branch.getBranchCode()));

        OrgLocationSite saved = repository.save(entity);
        log.info("Created LocationSite id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_UPDATE')")
    public ServiceResult<LocationSiteResponse> update(Long id, LocationSiteUpdateRequest request) {
        log.info("Updating LocationSite id={}", id);

        OrgLocationSite entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        Long branchId = entity.getBranch() != null ? entity.getBranch().getId() : null;
        if (repository.existsByNameArIgnoreCaseAndBranchIdAndIdNot(request.getNameAr(), branchId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndBranchIdAndIdNot(request.getNameEn(), branchId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        mapper.updateEntityFromRequest(entity, request);
        OrgLocationSite saved = repository.save(entity);
        log.info("Updated LocationSite id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_VIEW')")
    public ServiceResult<LocationSiteResponse> getById(Long id) {
        log.debug("Fetching LocationSite id={}", id);
        OrgLocationSite entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_VIEW')")
    public ServiceResult<Page<LocationSiteResponse>> search(LocationSiteSearchRequest searchRequest) {
        log.debug("Searching LocationSites");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgLocationSite> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgLocationSite> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_UPDATE')")
    public ServiceResult<LocationSiteResponse> activate(Long id) {
        log.info("Activating LocationSite id={}", id);
        OrgLocationSite entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_UPDATE')")
    public ServiceResult<LocationSiteResponse> deactivate(Long id) {
        log.info("Deactivating LocationSite id={}", id);
        OrgLocationSite entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.deactivate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_DELETE')")
    public void delete(Long id) {
        log.info("Deleting LocationSite id={}", id);
        OrgLocationSite entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        repository.delete(entity);
        log.info("Deleted LocationSite id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_VIEW')")
    public ServiceResult<LocationSiteUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for LocationSite id={}", id);
        OrgLocationSite entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toUsageResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LOCATION_SITE_VIEW')")
    public ServiceResult<List<LocationSiteOptionResponse>> listOptions(Long branchId) {
        log.debug("Listing LocationSite options for branchId={}", branchId);
        List<LocationSiteOptionResponse> options = repository.findAll().stream()
                .filter(ls -> Boolean.TRUE.equals(ls.getIsActiveFl())
                        && (branchId == null || (ls.getBranch() != null && branchId.equals(ls.getBranch().getId()))))
                .map(mapper::toOptionResponse)
                .toList();
        return ServiceResult.success(options);
    }
}
