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
import com.example.erp.org.domain.OrgBranchDomain;
import com.example.erp.org.dto.BranchCreateRequest;
import com.example.erp.org.dto.BranchResponse;
import com.example.erp.org.dto.BranchSearchRequest;
import com.example.erp.org.dto.BranchUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgLegalEntity;
import com.example.erp.org.exception.OrgErrorCodes;
import com.example.erp.org.mapper.BranchMapper;
import com.example.erp.org.repository.BranchRepository;
import com.example.erp.org.repository.CostCenterRepository;
import com.example.erp.org.repository.DepartmentRepository;
import com.example.erp.org.repository.LegalEntityRepository;
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
 * Orchestration for Branch (API-ORG-007..012). Business Rule decisions (RULE-ORG-018 parent-active
 * create guard; RULE-ORG-003/004/005 deactivation guards) are delegated to {@link OrgBranchDomain}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final BranchRepository branchRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final DepartmentRepository departmentRepository;
    private final CostCenterRepository costCenterRepository;
    private final LocationSiteRepository locationSiteRepository;
    private final BranchMapper branchMapper;
    private final OrgNumberGenerator orgNumberGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "branchCode", "nameAr", "nameEn", "legalEntity.id", "branchTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).BRANCH_CREATE)")
    public ServiceResult<BranchResponse> create(BranchCreateRequest request) {
        log.info("Creating Branch under LegalEntity ID: {}", request.getLegalEntityFk());

        OrgLegalEntity parent = legalEntityRepository.findById(request.getLegalEntityFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "LegalEntity", request.getLegalEntityFk()));

        assertNameNotDuplicate(parent.getId(), request.getNameAr(), request.getNameEn(), null);

        String generatedCode = orgNumberGenerator.next(
            "BR-" + parent.getLegalEntityCode() + "-",
            branchRepository.countByLegalEntity_Id(parent.getId()),
            code -> branchRepository.existsByLegalEntity_IdAndBranchCode(parent.getId(), code));
        OrgBranchDomain.create(generatedCode, Boolean.TRUE.equals(parent.getIsActiveFl()));

        OrgBranch entity = branchMapper.toEntity(request, generatedCode, parent);
        OrgBranch saved = branchRepository.save(entity);

        log.info("Created Branch ID: {}, code: {}", saved.getId(), saved.getBranchCode());
        return ServiceResult.success(branchMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).BRANCH_UPDATE)")
    public ServiceResult<BranchResponse> update(Long id, BranchUpdateRequest request) {
        log.info("Updating Branch ID: {}", id);

        OrgBranch entity = findOrThrow(id);
        assertNameNotDuplicate(entity.getLegalEntity().getId(), request.getNameAr(), request.getNameEn(), id);

        branchMapper.updateEntityFromRequest(entity, request);
        OrgBranch saved = branchRepository.save(entity);

        log.info("Updated Branch ID: {}", saved.getId());
        return ServiceResult.success(branchMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).BRANCH_VIEW)")
    public ServiceResult<BranchResponse> getById(Long id) {
        log.debug("Fetching Branch ID: {}", id);
        return ServiceResult.success(branchMapper.toResponse(findOrThrow(id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).BRANCH_VIEW)")
    public ServiceResult<Page<BranchResponse>> search(BranchSearchRequest searchRequest) {
        log.debug("Searching Branch");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        AllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgBranch> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<OrgBranch> page = branchRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(branchMapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).BRANCH_UPDATE)")
    public ServiceResult<BranchResponse> activate(Long id) {
        log.info("Activating Branch ID: {}", id);

        OrgBranch entity = findOrThrow(id);
        entity.activate();
        OrgBranch saved = branchRepository.save(entity);

        log.info("Activated Branch ID: {}", saved.getId());
        return ServiceResult.success(branchMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).BRANCH_UPDATE)")
    public ServiceResult<BranchResponse> deactivate(Long id) {
        log.info("Deactivating Branch ID: {}", id);

        OrgBranch entity = findOrThrow(id);

        long activeDepartmentCount = departmentRepository.countByBranch_IdAndIsActiveFlTrue(id);
        long activeCostCenterCount = costCenterRepository.countByBranch_IdAndIsActiveFlTrue(id);
        long activeLocationSiteCount = locationSiteRepository.countByBranch_IdAndIsActiveFlTrue(id);
        OrgBranchDomain.from(entity).assertCanDeactivate(activeDepartmentCount, activeCostCenterCount, activeLocationSiteCount);

        entity.deactivate();
        OrgBranch saved = branchRepository.save(entity);

        log.info("Deactivated Branch ID: {}", saved.getId());
        return ServiceResult.success(branchMapper.toResponse(saved), Status.UPDATED);
    }

    private OrgBranch findOrThrow(Long id) {
        return branchRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "Branch", id));
    }

    /** RULE-ORG-015 — name uniqueness within the parent LegalEntity scope. {@code excludeId} null on create. */
    private void assertNameNotDuplicate(Long legalEntityId, String nameAr, String nameEn, Long excludeId) {
        if (nameAr != null) {
            boolean taken = excludeId == null
                ? branchRepository.existsByLegalEntity_IdAndNameAr(legalEntityId, nameAr)
                : branchRepository.existsByLegalEntity_IdAndNameArAndIdNot(legalEntityId, nameAr, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameAr);
            }
        }
        if (nameEn != null) {
            boolean taken = excludeId == null
                ? branchRepository.existsByLegalEntity_IdAndNameEn(legalEntityId, nameEn)
                : branchRepository.existsByLegalEntity_IdAndNameEnAndIdNot(legalEntityId, nameEn, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameEn);
            }
        }
    }
}
