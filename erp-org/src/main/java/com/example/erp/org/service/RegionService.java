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
import com.example.erp.org.domain.OrgRegionDomain;
import com.example.erp.org.dto.RegionCreateRequest;
import com.example.erp.org.dto.RegionResponse;
import com.example.erp.org.dto.RegionSearchRequest;
import com.example.erp.org.dto.RegionUpdateRequest;
import com.example.erp.org.entity.OrgLegalEntity;
import com.example.erp.org.entity.OrgRegion;
import com.example.erp.org.entity.OrgRegionType;
import com.example.erp.org.exception.OrgErrorCodes;
import com.example.erp.org.mapper.RegionMapper;
import com.example.erp.org.repository.LegalEntityRepository;
import com.example.erp.org.repository.RegionRepository;
import com.example.erp.org.repository.RegionTypeRepository;
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
 * Orchestration for Region (API-ORG-013..018). Business Rule decisions are delegated to
 * {@link OrgRegionDomain}.
 *
 * TODO: OQ-001 — RULE-ORG-006 (block deactivation while active Branches reference this Region)
 * is pending resolution: db-script.md's DBF matrix has no REGION_FK column on ORG_BRANCH, so the
 * guard cannot be implemented against the current schema. {@code deactivate()} below does not
 * call a guard for this reason — {@link OrgRegionDomain} intentionally has no
 * {@code assertCanDeactivate} method. Not enforced until the FK linkage is confirmed at a
 * MODE 1.5 amendment.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegionService {

    private final RegionRepository regionRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final RegionMapper regionMapper;
    private final OrgNumberGenerator orgNumberGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "regionCode", "nameAr", "nameEn", "legalEntity.id", "regionType.id", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).REGION_CREATE)")
    public ServiceResult<RegionResponse> create(RegionCreateRequest request) {
        log.info("Creating Region under LegalEntity ID: {}", request.getLegalEntityFk());

        OrgLegalEntity parent = legalEntityRepository.findById(request.getLegalEntityFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "LegalEntity", request.getLegalEntityFk()));
        OrgRegionType regionType = regionTypeRepository.findById(request.getRegionTypeIdFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "RegionType", request.getRegionTypeIdFk()));

        assertNameNotDuplicate(parent.getId(), request.getNameAr(), request.getNameEn(), null);

        String generatedCode = orgNumberGenerator.next(
            "RG-" + parent.getLegalEntityCode() + "-",
            regionRepository.countByLegalEntity_Id(parent.getId()),
            code -> regionRepository.existsByLegalEntity_IdAndRegionCode(parent.getId(), code));
        OrgRegionDomain.create(generatedCode);

        OrgRegion entity = regionMapper.toEntity(request, generatedCode, parent, regionType);
        OrgRegion saved = regionRepository.save(entity);

        log.info("Created Region ID: {}, code: {}", saved.getId(), saved.getRegionCode());
        return ServiceResult.success(regionMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).REGION_UPDATE)")
    public ServiceResult<RegionResponse> update(Long id, RegionUpdateRequest request) {
        log.info("Updating Region ID: {}", id);

        OrgRegion entity = findOrThrow(id);
        assertNameNotDuplicate(entity.getLegalEntity().getId(), request.getNameAr(), request.getNameEn(), id);

        regionMapper.updateEntityFromRequest(entity, request);
        OrgRegion saved = regionRepository.save(entity);

        log.info("Updated Region ID: {}", saved.getId());
        return ServiceResult.success(regionMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).REGION_VIEW)")
    public ServiceResult<RegionResponse> getById(Long id) {
        log.debug("Fetching Region ID: {}", id);
        return ServiceResult.success(regionMapper.toResponse(findOrThrow(id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).REGION_VIEW)")
    public ServiceResult<Page<RegionResponse>> search(RegionSearchRequest searchRequest) {
        log.debug("Searching Region");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        AllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgRegion> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<OrgRegion> page = regionRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(regionMapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).REGION_UPDATE)")
    public ServiceResult<RegionResponse> activate(Long id) {
        log.info("Activating Region ID: {}", id);

        OrgRegion entity = findOrThrow(id);
        entity.activate();
        OrgRegion saved = regionRepository.save(entity);

        log.info("Activated Region ID: {}", saved.getId());
        return ServiceResult.success(regionMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).REGION_UPDATE)")
    public ServiceResult<RegionResponse> deactivate(Long id) {
        log.info("Deactivating Region ID: {}", id);

        // TODO: OQ-001 DEFERRED — RULE-ORG-006 active-Branches guard not enforced (see class Javadoc)
        OrgRegion entity = findOrThrow(id);
        entity.deactivate();
        OrgRegion saved = regionRepository.save(entity);

        log.info("Deactivated Region ID: {}", saved.getId());
        return ServiceResult.success(regionMapper.toResponse(saved), Status.UPDATED);
    }

    private OrgRegion findOrThrow(Long id) {
        return regionRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "Region", id));
    }

    /** RULE-ORG-015 — name uniqueness within the parent LegalEntity scope. {@code excludeId} null on create. */
    private void assertNameNotDuplicate(Long legalEntityId, String nameAr, String nameEn, Long excludeId) {
        if (nameAr != null) {
            boolean taken = excludeId == null
                ? regionRepository.existsByLegalEntity_IdAndNameAr(legalEntityId, nameAr)
                : regionRepository.existsByLegalEntity_IdAndNameArAndIdNot(legalEntityId, nameAr, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameAr);
            }
        }
        if (nameEn != null) {
            boolean taken = excludeId == null
                ? regionRepository.existsByLegalEntity_IdAndNameEn(legalEntityId, nameEn)
                : regionRepository.existsByLegalEntity_IdAndNameEnAndIdNot(legalEntityId, nameEn, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameEn);
            }
        }
    }
}
