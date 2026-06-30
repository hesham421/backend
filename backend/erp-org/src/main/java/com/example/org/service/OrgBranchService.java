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
import com.example.org.domain.OrgLegalEntity;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgBranchMapper;
import com.example.org.numbering.OrgCodeGenerator;
import com.example.org.repository.OrgBranchRepository;
import com.example.org.repository.OrgLegalEntityRepository;
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
public class OrgBranchService {

    private final OrgBranchRepository repository;
    private final OrgLegalEntityRepository legalEntityRepository;
    private final OrgBranchMapper mapper;
    private final OrgCodeGenerator codeGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "branchCode", "nameAr", "nameEn", "branchTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_BRANCH_CREATE')")
    public ServiceResult<BranchResponse> create(BranchCreateRequest request) {
        log.info("Creating Branch nameEn={} under legalEntityId={}", request.getNameEn(), request.getLegalEntityId());

        OrgLegalEntity legalEntity = legalEntityRepository.findById(request.getLegalEntityId())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getLegalEntityId()));

        if (!Boolean.TRUE.equals(legalEntity.getIsActiveFl())) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.INACTIVE_LEGAL_ENTITY, request.getLegalEntityId());
        }

        if (repository.existsByNameArIgnoreCaseAndLegalEntityId(request.getNameAr(), request.getLegalEntityId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndLegalEntityId(request.getNameEn(), request.getLegalEntityId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        OrgBranch entity = mapper.toEntity(request, legalEntity);
        entity.setBranchCode(codeGenerator.generateBranchCode(legalEntity.getLegalEntityCode()));

        OrgBranch saved = repository.save(entity);
        log.info("Created Branch id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_BRANCH_UPDATE')")
    public ServiceResult<BranchResponse> update(Long id, BranchUpdateRequest request) {
        log.info("Updating Branch id={}", id);

        OrgBranch entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        Long leId = entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null;
        if (repository.existsByNameArIgnoreCaseAndLegalEntityIdAndIdNot(request.getNameAr(), leId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndLegalEntityIdAndIdNot(request.getNameEn(), leId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        mapper.updateEntityFromRequest(entity, request);
        OrgBranch saved = repository.save(entity);
        log.info("Updated Branch id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BRANCH_VIEW')")
    public ServiceResult<BranchResponse> getById(Long id) {
        log.debug("Fetching Branch id={}", id);
        OrgBranch entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BRANCH_VIEW')")
    public ServiceResult<Page<BranchResponse>> search(BranchSearchRequest searchRequest) {
        log.debug("Searching Branches");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgBranch> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgBranch> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_BRANCH_UPDATE')")
    public ServiceResult<BranchResponse> activate(Long id) {
        log.info("Activating Branch id={}", id);
        OrgBranch entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_BRANCH_UPDATE')")
    public ServiceResult<BranchResponse> deactivate(Long id) {
        log.info("Deactivating Branch id={}", id);
        OrgBranch entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeDepts = repository.countActiveDepartments(id);
        if (activeDepts > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.BR_HAS_ACTIVE_DEPARTMENTS, id);
        }
        long activeCcs = repository.countActiveCostCenters(id);
        if (activeCcs > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.BR_HAS_ACTIVE_COST_CENTERS, id);
        }
        long activeLss = repository.countActiveLocationSites(id);
        if (activeLss > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.BR_HAS_ACTIVE_LOCATION_SITES, id);
        }

        entity.deactivate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_BRANCH_DELETE')")
    public void delete(Long id) {
        log.info("Deleting Branch id={}", id);
        OrgBranch entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeDepts = repository.countActiveDepartments(id);
        if (activeDepts > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.BR_HAS_ACTIVE_DEPARTMENTS, id);
        }
        long activeCcs = repository.countActiveCostCenters(id);
        if (activeCcs > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.BR_HAS_ACTIVE_COST_CENTERS, id);
        }
        long activeLss = repository.countActiveLocationSites(id);
        if (activeLss > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.BR_HAS_ACTIVE_LOCATION_SITES, id);
        }

        repository.delete(entity);
        log.info("Deleted Branch id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BRANCH_VIEW')")
    public ServiceResult<BranchUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for Branch id={}", id);
        OrgBranch entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        long activeDepts = repository.countActiveDepartments(id);
        long activeCcs = repository.countActiveCostCenters(id);
        long activeLss = repository.countActiveLocationSites(id);
        return ServiceResult.success(mapper.toUsageResponse(entity, activeDepts, activeCcs, activeLss));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_BRANCH_VIEW')")
    public ServiceResult<List<BranchOptionResponse>> listOptions(Long legalEntityId) {
        log.debug("Listing Branch options for legalEntityId={}", legalEntityId);
        List<BranchOptionResponse> options = repository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActiveFl())
                        && (legalEntityId == null || (b.getLegalEntity() != null && legalEntityId.equals(b.getLegalEntity().getId()))))
                .map(mapper::toOptionResponse)
                .toList();
        return ServiceResult.success(options);
    }
}
