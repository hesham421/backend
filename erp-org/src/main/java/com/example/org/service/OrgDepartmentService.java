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
import com.example.org.domain.OrgDepartment;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgDepartmentMapper;
import com.example.org.numbering.OrgCodeGenerator;
import com.example.org.repository.OrgBranchRepository;
import com.example.org.repository.OrgDepartmentRepository;
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
public class OrgDepartmentService {

    private final OrgDepartmentRepository repository;
    private final OrgBranchRepository branchRepository;
    private final OrgDepartmentMapper mapper;
    private final OrgCodeGenerator codeGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "departmentCode", "nameAr", "nameEn", "nodeTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_CREATE')")
    public ServiceResult<DepartmentResponse> create(DepartmentCreateRequest request) {
        log.info("Creating Department nameEn={} under branchId={}", request.getNameEn(), request.getBranchId());

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

        OrgDepartment parent = null;
        if (request.getParentId() != null) {
            parent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getParentId()));
        }

        OrgDepartment entity = mapper.toEntity(request, branch, parent);
        entity.setDepartmentCode(codeGenerator.generateDepartmentCode(branch.getBranchCode()));

        OrgDepartment saved = repository.save(entity);
        log.info("Created Department id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_UPDATE')")
    public ServiceResult<DepartmentResponse> update(Long id, DepartmentUpdateRequest request) {
        log.info("Updating Department id={}", id);

        OrgDepartment entity = repository.findById(id)
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
            OrgDepartment newParent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getParentId()));
            entity.setParentDepartment(newParent);
        } else {
            entity.setParentDepartment(null);
        }

        OrgDepartment saved = repository.save(entity);
        log.info("Updated Department id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_VIEW')")
    public ServiceResult<DepartmentResponse> getById(Long id) {
        log.debug("Fetching Department id={}", id);
        OrgDepartment entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_VIEW')")
    public ServiceResult<Page<DepartmentResponse>> search(DepartmentSearchRequest searchRequest) {
        log.debug("Searching Departments");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgDepartment> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgDepartment> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_UPDATE')")
    public ServiceResult<DepartmentResponse> activate(Long id) {
        log.info("Activating Department id={}", id);
        OrgDepartment entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_UPDATE')")
    public ServiceResult<DepartmentResponse> deactivate(Long id) {
        log.info("Deactivating Department id={}", id);
        OrgDepartment entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeChildren = repository.countActiveChildren(id);
        if (activeChildren > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.DEPT_CIRCULAR_REFERENCE, id);
        }

        entity.deactivate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_DELETE')")
    public void delete(Long id) {
        log.info("Deleting Department id={}", id);
        OrgDepartment entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeChildren = repository.countActiveChildren(id);
        if (activeChildren > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.DEPT_CIRCULAR_REFERENCE, id);
        }

        repository.delete(entity);
        log.info("Deleted Department id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_VIEW')")
    public ServiceResult<DepartmentUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for Department id={}", id);
        OrgDepartment entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        long activeChildren = repository.countActiveChildren(id);
        return ServiceResult.success(mapper.toUsageResponse(entity, activeChildren));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_VIEW')")
    public ServiceResult<List<DepartmentOptionResponse>> listOptions(Long branchId) {
        log.debug("Listing Department options for branchId={}", branchId);
        List<DepartmentOptionResponse> options = (branchId != null
                ? repository.findRootsByBranchId(branchId)
                : repository.findAll().stream().filter(d -> Boolean.TRUE.equals(d.getIsActiveFl())).toList())
                .stream().map(mapper::toOptionResponse).toList();
        return ServiceResult.success(options);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_DEPARTMENT_VIEW')")
    public ServiceResult<List<DepartmentTreeNodeResponse>> getTree(Long branchId, Boolean isActiveFl) {
        log.debug("Building Department tree for branchId={} isActiveFl={}", branchId, isActiveFl);
        List<OrgDepartment> flat = isActiveFl != null
                ? repository.findFlatByBranchIdAndIsActiveFl(branchId, isActiveFl)
                : repository.findFlatByBranchId(branchId);
        return ServiceResult.success(assembleDepartmentTree(flat));
    }

    private List<DepartmentTreeNodeResponse> assembleDepartmentTree(List<OrgDepartment> flat) {
        Map<Long, DepartmentTreeNodeResponse> nodeMap = new LinkedHashMap<>();
        for (OrgDepartment d : flat) {
            nodeMap.put(d.getId(), DepartmentTreeNodeResponse.builder()
                    .id(d.getId())
                    .code(d.getDepartmentCode())
                    .nameAr(d.getNameAr())
                    .nameEn(d.getNameEn())
                    .nodeType(d.getNodeTypeId())
                    .children(new ArrayList<>())
                    .build());
        }
        List<DepartmentTreeNodeResponse> roots = new ArrayList<>();
        for (OrgDepartment d : flat) {
            DepartmentTreeNodeResponse node = nodeMap.get(d.getId());
            if (d.getParentDepartment() == null) {
                roots.add(node);
            } else {
                DepartmentTreeNodeResponse parent = nodeMap.get(d.getParentDepartment().getId());
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
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.DEPT_CIRCULAR_REFERENCE, entityId);
        }
        Set<Long> visited = new HashSet<>();
        visited.add(entityId);
        Long currentId = newParentId;
        while (currentId != null) {
            if (visited.contains(currentId)) {
                throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.DEPT_CIRCULAR_REFERENCE, entityId);
            }
            visited.add(currentId);
            OrgDepartment current = repository.findById(currentId).orElse(null);
            currentId = (current != null && current.getParentDepartment() != null) ? current.getParentDepartment().getId() : null;
        }
    }
}
