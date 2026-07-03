package com.example.erp.org.service;

import com.erp.common.search.AllowedFields;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.org.domain.OrgCostCenterDomain;
import com.example.erp.org.dto.CostCenterCreateRequest;
import com.example.erp.org.dto.CostCenterResponse;
import com.example.erp.org.dto.CostCenterSearchRequest;
import com.example.erp.org.dto.CostCenterTreeNodeResponse;
import com.example.erp.org.dto.CostCenterUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgCostCenter;
import com.example.erp.org.exception.OrgErrorCodes;
import com.example.erp.org.mapper.CostCenterMapper;
import com.example.erp.org.repository.BranchRepository;
import com.example.erp.org.repository.CostCenterRepository;
import com.example.erp.org.service.support.OrgNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orchestration for CostCenter flat CRUD (API-ORG-026,028..032) and the recursive tree read
 * (API-ORG-027, QR-ORG-015, mirrors QR-ORG-012). Business Rule decisions (RULE-ORG-019
 * parent-active create guard; RULE-ORG-008 cycle prevention) are delegated to
 * {@link OrgCostCenterDomain}. RULE-ORG-010 (SUMMARY blocked on transactional records) is
 * enforced by consuming modules, not here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostCenterService {

    private static final int MAX_TREE_DEPTH = 100;

    private final CostCenterRepository costCenterRepository;
    private final BranchRepository branchRepository;
    private final CostCenterMapper costCenterMapper;
    private final OrgNumberGenerator orgNumberGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "costCenterCode", "nameAr", "nameEn", "branch.id", "nodeTypeId", "costCenterTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).COST_CENTER_CREATE)")
    public ServiceResult<CostCenterResponse> create(CostCenterCreateRequest request) {
        log.info("Creating CostCenter under Branch ID: {}", request.getBranchFk());

        OrgBranch parent = branchRepository.findById(request.getBranchFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "Branch", request.getBranchFk()));

        assertNameNotDuplicate(parent.getId(), request.getNameAr(), request.getNameEn(), null);

        String generatedCode = orgNumberGenerator.next(
            "CC-" + orgNumberGenerator.parentSuffix(parent.getBranchCode()) + "-",
            costCenterRepository.countByBranch_Id(parent.getId()),
            code -> costCenterRepository.existsByBranch_IdAndCostCenterCode(parent.getId(), code));
        OrgCostCenterDomain.create(generatedCode, Boolean.TRUE.equals(parent.getIsActiveFl()));

        OrgCostCenter parentCostCenter = request.getParentCostCenterFk() != null
            ? findOrThrow(request.getParentCostCenterFk())
            : null;

        OrgCostCenter entity = costCenterMapper.toEntity(request, generatedCode, parent, parentCostCenter);
        OrgCostCenter saved = costCenterRepository.save(entity);

        log.info("Created CostCenter ID: {}, code: {}", saved.getId(), saved.getCostCenterCode());
        return ServiceResult.success(costCenterMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).COST_CENTER_UPDATE)")
    public ServiceResult<CostCenterResponse> update(Long id, CostCenterUpdateRequest request) {
        log.info("Updating CostCenter ID: {}", id);

        OrgCostCenter entity = findOrThrow(id);
        assertNameNotDuplicate(entity.getBranch().getId(), request.getNameAr(), request.getNameEn(), id);

        if (request.getParentCostCenterFk() != null) {
            Set<Long> ancestorIds = collectAncestorIds(request.getParentCostCenterFk());
            OrgCostCenterDomain.from(entity).assertNoCycle(entity.getId(), request.getParentCostCenterFk(), ancestorIds);
            entity.setParentCostCenter(findOrThrow(request.getParentCostCenterFk()));
        }

        costCenterMapper.updateEntityFromRequest(entity, request);
        OrgCostCenter saved = costCenterRepository.save(entity);

        log.info("Updated CostCenter ID: {}", saved.getId());
        return ServiceResult.success(costCenterMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).COST_CENTER_VIEW)")
    public ServiceResult<CostCenterResponse> getById(Long id) {
        log.debug("Fetching CostCenter ID: {}", id);
        return ServiceResult.success(costCenterMapper.toResponse(findOrThrow(id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).COST_CENTER_VIEW)")
    public ServiceResult<Page<CostCenterResponse>> search(CostCenterSearchRequest searchRequest) {
        log.debug("Searching CostCenter");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        AllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgCostCenter> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<OrgCostCenter> page = costCenterRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(costCenterMapper::toResponse));
    }

    /**
     * API-ORG-027 — full subtree for {@code branchFk} (QR-ORG-015). Fetches the flat row set from
     * the recursive CTE, then assembles the nested tree in this layer (per SVC-API-TREE.md — the
     * DB returns rows, not nesting). {@code isActiveFl} is optional; when a node's parent was
     * filtered out at the DB level, the node surfaces as a root of the returned forest.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).COST_CENTER_VIEW)")
    public ServiceResult<List<CostCenterTreeNodeResponse>> getTree(Long branchFk, Boolean isActiveFl) {
        log.debug("Fetching CostCenter tree for Branch ID: {}", branchFk);

        List<OrgCostCenter> flat = costCenterRepository.findTreeByBranch(
            branchFk, BooleanNumberConverter.toDbValue(isActiveFl));

        Map<Long, CostCenterTreeNodeResponse> nodesById = new LinkedHashMap<>();
        for (OrgCostCenter entity : flat) {
            nodesById.put(entity.getId(), CostCenterTreeNodeResponse.builder()
                .id(entity.getId())
                .code(entity.getCostCenterCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .nodeTypeId(entity.getNodeTypeId())
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .build());
        }

        List<CostCenterTreeNodeResponse> roots = new ArrayList<>();
        for (OrgCostCenter entity : flat) {
            CostCenterTreeNodeResponse node = nodesById.get(entity.getId());
            Long parentId = entity.getParentCostCenter() != null ? entity.getParentCostCenter().getId() : null;
            if (parentId != null && nodesById.containsKey(parentId)) {
                nodesById.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }

        return ServiceResult.success(roots);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).COST_CENTER_UPDATE)")
    public ServiceResult<CostCenterResponse> activate(Long id) {
        log.info("Activating CostCenter ID: {}", id);

        OrgCostCenter entity = findOrThrow(id);
        entity.activate();
        OrgCostCenter saved = costCenterRepository.save(entity);

        log.info("Activated CostCenter ID: {}", saved.getId());
        return ServiceResult.success(costCenterMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).COST_CENTER_UPDATE)")
    public ServiceResult<CostCenterResponse> deactivate(Long id) {
        log.info("Deactivating CostCenter ID: {}", id);

        // No entity-specific deactivation guard for CostCenter (per SRS A6 lifecycle table) —
        // RULE-ORG-010 (SUMMARY blocked on transactional records) is enforced by consuming modules.
        OrgCostCenter entity = findOrThrow(id);
        entity.deactivate();
        OrgCostCenter saved = costCenterRepository.save(entity);

        log.info("Deactivated CostCenter ID: {}", saved.getId());
        return ServiceResult.success(costCenterMapper.toResponse(saved), Status.UPDATED);
    }

    private OrgCostCenter findOrThrow(Long id) {
        return costCenterRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "CostCenter", id));
    }

    /** RULE-ORG-015 — name uniqueness within the parent Branch scope. {@code excludeId} null on create. */
    private void assertNameNotDuplicate(Long branchId, String nameAr, String nameEn, Long excludeId) {
        if (nameAr != null) {
            boolean taken = excludeId == null
                ? costCenterRepository.existsByBranch_IdAndNameAr(branchId, nameAr)
                : costCenterRepository.existsByBranch_IdAndNameArAndIdNot(branchId, nameAr, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameAr);
            }
        }
        if (nameEn != null) {
            boolean taken = excludeId == null
                ? costCenterRepository.existsByBranch_IdAndNameEn(branchId, nameEn)
                : costCenterRepository.existsByBranch_IdAndNameEnAndIdNot(branchId, nameEn, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameEn);
            }
        }
    }

    /**
     * Walks the proposed parent's own ancestor chain (including itself) up to the root, for
     * {@link OrgCostCenterDomain#assertNoCycle}. The Domain never touches the Repository — this
     * fetch stays in the Service (RULE-ORG-008).
     */
    private Set<Long> collectAncestorIds(Long proposedParentId) {
        Set<Long> ancestorIds = new LinkedHashSet<>();
        Long currentId = proposedParentId;
        int depth = 0;
        while (currentId != null && depth++ < MAX_TREE_DEPTH) {
            if (!ancestorIds.add(currentId)) {
                break; // already-visited node — existing data cycle, stop walking
            }
            OrgCostCenter current = costCenterRepository.findById(currentId).orElse(null);
            currentId = (current != null && current.getParentCostCenter() != null)
                ? current.getParentCostCenter().getId()
                : null;
        }
        return ancestorIds;
    }
}
