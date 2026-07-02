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
import com.example.erp.org.domain.OrgDepartmentDomain;
import com.example.erp.org.dto.DepartmentCreateRequest;
import com.example.erp.org.dto.DepartmentResponse;
import com.example.erp.org.dto.DepartmentSearchRequest;
import com.example.erp.org.dto.DepartmentTreeNodeResponse;
import com.example.erp.org.dto.DepartmentUpdateRequest;
import com.example.erp.org.entity.OrgBranch;
import com.example.erp.org.entity.OrgDepartment;
import com.example.erp.org.exception.OrgErrorCodes;
import com.example.erp.org.mapper.DepartmentMapper;
import com.example.erp.org.repository.BranchRepository;
import com.example.erp.org.repository.DepartmentRepository;
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
 * Orchestration for Department flat CRUD (API-ORG-019,021..025) and the recursive tree read
 * (API-ORG-020, QR-ORG-012). Business Rule decisions (RULE-ORG-019 parent-active create guard;
 * RULE-ORG-007 cycle prevention) are delegated to {@link OrgDepartmentDomain}. RULE-ORG-009
 * (SUMMARY blocked on transactional records) is enforced by consuming modules, not here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private static final int MAX_TREE_DEPTH = 100;

    private final DepartmentRepository departmentRepository;
    private final BranchRepository branchRepository;
    private final DepartmentMapper departmentMapper;
    private final OrgNumberGenerator orgNumberGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "departmentCode", "nameAr", "nameEn", "branch.id", "nodeTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).DEPARTMENT_CREATE)")
    public ServiceResult<DepartmentResponse> create(DepartmentCreateRequest request) {
        log.info("Creating Department under Branch ID: {}", request.getBranchFk());

        OrgBranch parent = branchRepository.findById(request.getBranchFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "Branch", request.getBranchFk()));

        assertNameNotDuplicate(parent.getId(), request.getNameAr(), request.getNameEn(), null);

        String generatedCode = orgNumberGenerator.next(
            "DEP-" + parent.getBranchCode() + "-",
            departmentRepository.countByBranch_Id(parent.getId()),
            code -> departmentRepository.existsByBranch_IdAndDepartmentCode(parent.getId(), code));
        OrgDepartmentDomain.create(generatedCode, Boolean.TRUE.equals(parent.getIsActiveFl()));

        OrgDepartment parentDepartment = request.getParentDepartmentFk() != null
            ? findOrThrow(request.getParentDepartmentFk())
            : null;

        OrgDepartment entity = departmentMapper.toEntity(request, generatedCode, parent, parentDepartment);
        OrgDepartment saved = departmentRepository.save(entity);

        log.info("Created Department ID: {}, code: {}", saved.getId(), saved.getDepartmentCode());
        return ServiceResult.success(departmentMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).DEPARTMENT_UPDATE)")
    public ServiceResult<DepartmentResponse> update(Long id, DepartmentUpdateRequest request) {
        log.info("Updating Department ID: {}", id);

        OrgDepartment entity = findOrThrow(id);
        assertNameNotDuplicate(entity.getBranch().getId(), request.getNameAr(), request.getNameEn(), id);

        if (request.getParentDepartmentFk() != null) {
            Set<Long> ancestorIds = collectAncestorIds(request.getParentDepartmentFk());
            OrgDepartmentDomain.from(entity).assertNoCycle(entity.getId(), request.getParentDepartmentFk(), ancestorIds);
            entity.setParentDepartment(findOrThrow(request.getParentDepartmentFk()));
        }

        departmentMapper.updateEntityFromRequest(entity, request);
        OrgDepartment saved = departmentRepository.save(entity);

        log.info("Updated Department ID: {}", saved.getId());
        return ServiceResult.success(departmentMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).DEPARTMENT_VIEW)")
    public ServiceResult<DepartmentResponse> getById(Long id) {
        log.debug("Fetching Department ID: {}", id);
        return ServiceResult.success(departmentMapper.toResponse(findOrThrow(id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).DEPARTMENT_VIEW)")
    public ServiceResult<Page<DepartmentResponse>> search(DepartmentSearchRequest searchRequest) {
        log.debug("Searching Department");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        AllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgDepartment> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<OrgDepartment> page = departmentRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(departmentMapper::toResponse));
    }

    /**
     * API-ORG-020 — full subtree for {@code branchFk} (QR-ORG-012). Fetches the flat row set from
     * the recursive CTE, then assembles the nested tree in this layer (per SVC-API-TREE.md — the
     * DB returns rows, not nesting). {@code isActiveFl} is optional; when a node's parent was
     * filtered out at the DB level, the node surfaces as a root of the returned forest.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).DEPARTMENT_VIEW)")
    public ServiceResult<List<DepartmentTreeNodeResponse>> getTree(Long branchFk, Boolean isActiveFl) {
        log.debug("Fetching Department tree for Branch ID: {}", branchFk);

        List<OrgDepartment> flat = departmentRepository.findTreeByBranch(
            branchFk, BooleanNumberConverter.toDbValue(isActiveFl));

        Map<Long, DepartmentTreeNodeResponse> nodesById = new LinkedHashMap<>();
        for (OrgDepartment entity : flat) {
            nodesById.put(entity.getId(), DepartmentTreeNodeResponse.builder()
                .id(entity.getId())
                .code(entity.getDepartmentCode())
                .nameAr(entity.getNameAr())
                .nameEn(entity.getNameEn())
                .nodeTypeId(entity.getNodeTypeId())
                .isActive(Boolean.TRUE.equals(entity.getIsActiveFl()))
                .build());
        }

        List<DepartmentTreeNodeResponse> roots = new ArrayList<>();
        for (OrgDepartment entity : flat) {
            DepartmentTreeNodeResponse node = nodesById.get(entity.getId());
            Long parentId = entity.getParentDepartment() != null ? entity.getParentDepartment().getId() : null;
            if (parentId != null && nodesById.containsKey(parentId)) {
                nodesById.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }

        return ServiceResult.success(roots);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).DEPARTMENT_UPDATE)")
    public ServiceResult<DepartmentResponse> activate(Long id) {
        log.info("Activating Department ID: {}", id);

        OrgDepartment entity = findOrThrow(id);
        entity.activate();
        OrgDepartment saved = departmentRepository.save(entity);

        log.info("Activated Department ID: {}", saved.getId());
        return ServiceResult.success(departmentMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).DEPARTMENT_UPDATE)")
    public ServiceResult<DepartmentResponse> deactivate(Long id) {
        log.info("Deactivating Department ID: {}", id);

        // No entity-specific deactivation guard for Department (per SRS A6 lifecycle table) —
        // RULE-ORG-009 (SUMMARY blocked on transactional records) is enforced by consuming modules.
        OrgDepartment entity = findOrThrow(id);
        entity.deactivate();
        OrgDepartment saved = departmentRepository.save(entity);

        log.info("Deactivated Department ID: {}", saved.getId());
        return ServiceResult.success(departmentMapper.toResponse(saved), Status.UPDATED);
    }

    private OrgDepartment findOrThrow(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "Department", id));
    }

    /** RULE-ORG-015 — name uniqueness within the parent Branch scope. {@code excludeId} null on create. */
    private void assertNameNotDuplicate(Long branchId, String nameAr, String nameEn, Long excludeId) {
        if (nameAr != null) {
            boolean taken = excludeId == null
                ? departmentRepository.existsByBranch_IdAndNameAr(branchId, nameAr)
                : departmentRepository.existsByBranch_IdAndNameArAndIdNot(branchId, nameAr, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameAr);
            }
        }
        if (nameEn != null) {
            boolean taken = excludeId == null
                ? departmentRepository.existsByBranch_IdAndNameEn(branchId, nameEn)
                : departmentRepository.existsByBranch_IdAndNameEnAndIdNot(branchId, nameEn, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameEn);
            }
        }
    }

    /**
     * Walks the proposed parent's own ancestor chain (including itself) up to the root, for
     * {@link OrgDepartmentDomain#assertNoCycle}. The Domain never touches the Repository — this
     * fetch stays in the Service (RULE-ORG-007).
     */
    private Set<Long> collectAncestorIds(Long proposedParentId) {
        Set<Long> ancestorIds = new LinkedHashSet<>();
        Long currentId = proposedParentId;
        int depth = 0;
        while (currentId != null && depth++ < MAX_TREE_DEPTH) {
            if (!ancestorIds.add(currentId)) {
                break; // already-visited node — existing data cycle, stop walking
            }
            OrgDepartment current = departmentRepository.findById(currentId).orElse(null);
            currentId = (current != null && current.getParentDepartment() != null)
                ? current.getParentDepartment().getId()
                : null;
        }
        return ancestorIds;
    }
}
