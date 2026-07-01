package com.example.org.service;

import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.org.domain.OrgRegionType;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgRegionTypeMapper;
import com.example.org.repository.OrgRegionRepository;
import com.example.org.repository.OrgRegionTypeRepository;
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
public class OrgRegionTypeService {

    private final OrgRegionTypeRepository repository;
    private final OrgRegionRepository regionRepository;
    private final OrgRegionTypeMapper mapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "regionTypeCode", "nameAr", "nameEn", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_CREATE')")
    public ServiceResult<RegionTypeResponse> create(RegionTypeCreateRequest request) {
        log.info("Creating RegionType nameEn={}", request.getNameEn());

        if (repository.existsByNameArIgnoreCase(request.getNameAr())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCase(request.getNameEn())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        OrgRegionType entity = mapper.toEntity(request);
        entity.setRegionTypeCode("RT-" + String.format("%05d", repository.count() + 1));

        OrgRegionType saved = repository.save(entity);
        log.info("Created RegionType id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_UPDATE')")
    public ServiceResult<RegionTypeResponse> update(Long id, RegionTypeUpdateRequest request) {
        log.info("Updating RegionType id={}", id);

        OrgRegionType entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        if (repository.existsByNameArIgnoreCaseAndIdNot(request.getNameAr(), id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndIdNot(request.getNameEn(), id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        mapper.updateEntityFromRequest(entity, request);
        OrgRegionType saved = repository.save(entity);
        log.info("Updated RegionType id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_VIEW')")
    public ServiceResult<RegionTypeResponse> getById(Long id) {
        log.debug("Fetching RegionType id={}", id);
        OrgRegionType entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_VIEW')")
    public ServiceResult<Page<RegionTypeResponse>> search(RegionTypeSearchRequest searchRequest) {
        log.debug("Searching RegionTypes");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgRegionType> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgRegionType> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_UPDATE')")
    public ServiceResult<RegionTypeResponse> activate(Long id) {
        log.info("Activating RegionType id={}", id);
        OrgRegionType entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_UPDATE')")
    public ServiceResult<RegionTypeResponse> deactivate(Long id) {
        log.info("Deactivating RegionType id={}", id);
        OrgRegionType entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeRegionCount = regionRepository.countByRegionTypeId(id);
        if (activeRegionCount > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.RG_HAS_ACTIVE_BRANCHES, id);
        }

        entity.deactivate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_DELETE')")
    public void delete(Long id) {
        log.info("Deleting RegionType id={}", id);
        OrgRegionType entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeRegionCount = regionRepository.countByRegionTypeId(id);
        if (activeRegionCount > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.RG_HAS_ACTIVE_BRANCHES, id);
        }

        repository.delete(entity);
        log.info("Deleted RegionType id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_VIEW')")
    public ServiceResult<RegionTypeUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for RegionType id={}", id);
        OrgRegionType entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        long activeRegionCount = regionRepository.countByRegionTypeId(id);
        return ServiceResult.success(mapper.toUsageResponse(entity, activeRegionCount));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_TYPE_VIEW')")
    public ServiceResult<List<RegionTypeOptionResponse>> listOptions() {
        log.debug("Listing active RegionType options");
        List<RegionTypeOptionResponse> options = repository.findAllByIsActiveFl(Boolean.TRUE)
                .stream().map(mapper::toOptionResponse).toList();
        return ServiceResult.success(options);
    }
}
