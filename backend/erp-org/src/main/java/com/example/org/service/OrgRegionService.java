package com.example.org.service;

import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.org.domain.OrgLegalEntity;
import com.example.org.domain.OrgRegion;
import com.example.org.domain.OrgRegionType;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgRegionMapper;
import com.example.org.numbering.OrgCodeGenerator;
import com.example.org.repository.OrgLegalEntityRepository;
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
public class OrgRegionService {

    private final OrgRegionRepository repository;
    private final OrgLegalEntityRepository legalEntityRepository;
    private final OrgRegionTypeRepository regionTypeRepository;
    private final OrgRegionMapper mapper;
    private final OrgCodeGenerator codeGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "regionCode", "nameAr", "nameEn", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_CREATE')")
    public ServiceResult<RegionResponse> create(RegionCreateRequest request) {
        log.info("Creating Region nameEn={} under legalEntityId={}", request.getNameEn(), request.getLegalEntityId());

        OrgLegalEntity legalEntity = legalEntityRepository.findById(request.getLegalEntityId())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getLegalEntityId()));
        if (!Boolean.TRUE.equals(legalEntity.getIsActiveFl())) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.INACTIVE_LEGAL_ENTITY, request.getLegalEntityId());
        }

        OrgRegionType regionType = regionTypeRepository.findById(request.getRegionTypeId())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getRegionTypeId()));

        if (repository.existsByNameArIgnoreCaseAndLegalEntityId(request.getNameAr(), request.getLegalEntityId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndLegalEntityId(request.getNameEn(), request.getLegalEntityId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        OrgRegion entity = mapper.toEntity(request, legalEntity, regionType);
        entity.setRegionCode(codeGenerator.generateRegionCode(legalEntity.getLegalEntityCode()));

        OrgRegion saved = repository.save(entity);
        log.info("Created Region id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_UPDATE')")
    public ServiceResult<RegionResponse> update(Long id, RegionUpdateRequest request) {
        log.info("Updating Region id={}", id);

        OrgRegion entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        Long leId = entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null;
        if (repository.existsByNameArIgnoreCaseAndLegalEntityIdAndIdNot(request.getNameAr(), leId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndLegalEntityIdAndIdNot(request.getNameEn(), leId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        mapper.updateEntityFromRequest(entity, request);

        if (request.getRegionTypeId() != null) {
            OrgRegionType regionType = regionTypeRepository.findById(request.getRegionTypeId())
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getRegionTypeId()));
            entity.setRegionType(regionType);
        }

        OrgRegion saved = repository.save(entity);
        log.info("Updated Region id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_VIEW')")
    public ServiceResult<RegionResponse> getById(Long id) {
        log.debug("Fetching Region id={}", id);
        OrgRegion entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_VIEW')")
    public ServiceResult<Page<RegionResponse>> search(RegionSearchRequest searchRequest) {
        log.debug("Searching Regions");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgRegion> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgRegion> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_UPDATE')")
    public ServiceResult<RegionResponse> activate(Long id) {
        log.info("Activating Region id={}", id);
        // TODO: OQ-001 — RULE-ORG-006/017 guard pending resolution
        OrgRegion entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_UPDATE')")
    public ServiceResult<RegionResponse> deactivate(Long id) {
        log.info("Deactivating Region id={}", id);
        // TODO: OQ-001 — RULE-ORG-006/017 deactivation guard pending resolution
        OrgRegion entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.deactivate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_REGION_DELETE')")
    public void delete(Long id) {
        log.info("Deleting Region id={}", id);
        OrgRegion entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        repository.delete(entity);
        log.info("Deleted Region id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_VIEW')")
    public ServiceResult<RegionUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for Region id={}", id);
        OrgRegion entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toUsageResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_REGION_VIEW')")
    public ServiceResult<List<RegionOptionResponse>> listOptions(Long legalEntityId) {
        log.debug("Listing Region options for legalEntityId={}", legalEntityId);
        List<RegionOptionResponse> options = (legalEntityId != null
                ? repository.findActiveByLegalEntityId(legalEntityId)
                : repository.findAll().stream().filter(r -> Boolean.TRUE.equals(r.getIsActiveFl())).toList())
                .stream().map(mapper::toOptionResponse).toList();
        return ServiceResult.success(options);
    }
}
