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
import com.example.org.domain.OrgCostCenter;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgCostCenterMapper;
import com.example.org.numbering.OrgCodeGenerator;
import com.example.org.repository.OrgBranchRepository;
import com.example.org.repository.OrgCostCenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgCostCenterService {

    private final OrgCostCenterRepository repository;
    private final OrgBranchRepository branchRepository;
    private final OrgCostCenterMapper mapper;
    private final OrgCodeGenerator codeGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "costCenterCode", "nameAr", "nameEn", "nodeTypeId", "costCenterTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_CREATE')")
    public ServiceResult<CostCenterResponse> create(CostCenterCreateRequest request) {
        log.info("Creating CostCenter nameEn={} under branchId={}", request.getNameEn(), request.getBranchId());

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

        OrgCostCenter parent = null;
        if (request.getParentId() != null) {
            parent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getParentId()));
        }

        OrgCostCenter entity = mapper.toEntity(request, branch, parent);
        entity.setCostCenterCode(codeGenerator.generateCostCenterCode(branch.getBranchCode()));

        OrgCostCenter saved = repository.save(entity);
        log.info("Created CostCenter id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_UPDATE')")
    public ServiceResult<CostCenterResponse> update(Long id, CostCenterUpdateRequest request) {
        log.info("Updating CostCenter id={}", id);

        OrgCostCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        Long branchId = entity.getBranch() != null ? entity.getBranch().getId() : null;
        if (repository.existsByNameArIgnoreCaseAndBranchIdAndIdNot(request.getNameAr(), branchId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndBranchIdAndIdNot(request.getNameEn(), branchId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        mapper.updateEntityFromRequest(entity, request);

        if (request.getParentId() != null) {
            validateNoCircularRef(id, request.getParentId());
            OrgCostCenter newParent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getParentId()));
            entity.setParentCostCenter(newParent);
        } else {
            entity.setParentCostCenter(null);
        }

        OrgCostCenter saved = repository.save(entity);
        log.info("Updated CostCenter id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_VIEW')")
    public ServiceResult<CostCenterResponse> getById(Long id) {
        log.debug("Fetching CostCenter id={}", id);
        OrgCostCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_VIEW')")
    public ServiceResult<Page<CostCenterResponse>> search(CostCenterSearchRequest searchRequest) {
        log.debug("Searching CostCenters");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgCostCenter> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgCostCenter> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_UPDATE')")
    public ServiceResult<CostCenterResponse> activate(Long id) {
        log.info("Activating CostCenter id={}", id);
        OrgCostCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_UPDATE')")
    public ServiceResult<CostCenterResponse> deactivate(Long id) {
        log.info("Deactivating CostCenter id={}", id);
        OrgCostCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeChildren = repository.countActiveChildren(id);
        if (activeChildren > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.CC_CIRCULAR_REFERENCE, id);
        }

        entity.deactivate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_DELETE')")
    public void delete(Long id) {
        log.info("Deleting CostCenter id={}", id);
        OrgCostCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeChildren = repository.countActiveChildren(id);
        if (activeChildren > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.CC_CIRCULAR_REFERENCE, id);
        }

        repository.delete(entity);
        log.info("Deleted CostCenter id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_VIEW')")
    public ServiceResult<CostCenterUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for CostCenter id={}", id);
        OrgCostCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        long activeChildren = repository.countActiveChildren(id);
        return ServiceResult.success(mapper.toUsageResponse(entity, activeChildren));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_VIEW')")
    public ServiceResult<List<CostCenterOptionResponse>> listOptions(Long branchId) {
        log.debug("Listing CostCenter options for branchId={}", branchId);
        List<CostCenterOptionResponse> options = (branchId != null
                ? repository.findRootsByBranchId(branchId)
                : repository.findAll().stream().filter(c -> Boolean.TRUE.equals(c.getIsActiveFl())).toList())
                .stream().map(mapper::toOptionResponse).toList();
        return ServiceResult.success(options);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_COST_CENTER_VIEW')")
    public ServiceResult<List<CostCenterTreeNodeResponse>> getTree(Long branchId, Boolean isActiveFl) {
        log.debug("Building CostCenter tree for branchId={} isActiveFl={}", branchId, isActiveFl);
        List<OrgCostCenter> flat = isActiveFl != null
                ? repository.findFlatByBranchIdAndIsActiveFl(branchId, isActiveFl)
                : repository.findFlatByBranchId(branchId);
        return ServiceResult.success(assembleCostCenterTree(flat));
    }

    private List<CostCenterTreeNodeResponse> assembleCostCenterTree(List<OrgCostCenter> flat) {
        Map<Long, CostCenterTreeNodeResponse> nodeMap = new LinkedHashMap<>();
        for (OrgCostCenter c : flat) {
            nodeMap.put(c.getId(), CostCenterTreeNodeResponse.builder()
                    .id(c.getId())
                    .code(c.getCostCenterCode())
                    .nameAr(c.getNameAr())
                    .nameEn(c.getNameEn())
                    .nodeType(c.getNodeTypeId())
                    .children(new ArrayList<>())
                    .build());
        }
        List<CostCenterTreeNodeResponse> roots = new ArrayList<>();
        for (OrgCostCenter c : flat) {
            CostCenterTreeNodeResponse node = nodeMap.get(c.getId());
            if (c.getParentCostCenter() == null) {
                roots.add(node);
            } else {
                CostCenterTreeNodeResponse parent = nodeMap.get(c.getParentCostCenter().getId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    private void validateNoCircularRef(Long entityId, Long newParentId) {
        if (entityId.equals(newParentId)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.CC_CIRCULAR_REFERENCE, entityId);
        }
        Set<Long> visited = new HashSet<>();
        visited.add(entityId);
        Long currentId = newParentId;
        while (currentId != null) {
            if (visited.contains(currentId)) {
                throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.CC_CIRCULAR_REFERENCE, entityId);
            }
            visited.add(currentId);
            OrgCostCenter current = repository.findById(currentId).orElse(null);
            currentId = (current != null && current.getParentCostCenter() != null) ? current.getParentCostCenter().getId() : null;
        }
    }
}
