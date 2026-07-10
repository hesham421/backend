package com.example.security.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.common.web.util.PageableValidator;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.security.client.MasterDataLookupClient;
import com.example.security.dto.CreateSecRoleBranchRequest;
import com.example.security.dto.SecRoleBranchDto;
import com.example.security.dto.UpdateSecRoleBranchRequest;
import com.example.security.entity.SecRoleBranch;
import com.example.security.entity.SecRoleBranchId;
import com.example.security.exception.SecurityErrorCodes;
import com.example.security.mapper.SecRoleBranchMapper;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.SecRoleBranchRepository;
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
 * Service for SEC_ROLE_BRANCH CRUD (API-SEC-036..039, execution-plan-SEC-gaps.md Phase SVC+API).
 *
 * {@code @PreAuthorize} per Phase SEC (Section 8.1 Permissions Matrix) reuses the EXISTING
 * {@code PERM_ROLE_*} permissions — CORE-9, no new SEC_PAGES row/permission set for this sub-tab.
 *
 * Update/delete take (roleId, branchId) rather than a single {id}: SEC_ROLE_BRANCH has no
 * surrogate PK (composite key per execution-plan-SEC-gaps.md Section 3 / db-script-SEC-gaps.md
 * BLOCK 5a) — a single {id} path variable would require inventing a non-existent column,
 * which the governing high-precision rules prohibit. Flagged in the Phase 3 handoff as a
 * deliberate adaptation of the API register's literal "{id}" shorthand.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecRoleBranchService {

    private final SecRoleBranchRepository repo;
    private final RoleRepository roleRepo;
    private final MasterDataLookupClient lookupClient;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "roleIdFk", "branchIdFk", "dataAccessLevel", "isActiveFl", "createdAt"
    );
    private static final Set<String> ALLOWED_SEARCH_FIELDS = ALLOWED_SORT_FIELDS;

    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).ROLE_CREATE)")
    @Transactional
    public ServiceResult<SecRoleBranchDto> create(CreateSecRoleBranchRequest request) {
        log.info("Creating SEC_ROLE_BRANCH for role {} branch {}", request.getRoleIdFk(), request.getBranchIdFk());

        roleRepo.findById(request.getRoleIdFk())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.ROLE_NOT_FOUND, request.getRoleIdFk()));

        // RULE-SEC-036 — no duplicate (roleIdFk, branchIdFk)
        if (repo.existsByRoleIdFkAndBranchIdFk(request.getRoleIdFk(), request.getBranchIdFk())) {
            throw new LocalizedException(Status.CONFLICT, SecurityErrorCodes.SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT);
        }

        // RULE-SEC-035 — data access level required + validated against LOV-SEC-002
        assertValidDataAccessLevel(request.getDataAccessLevel());

        SecRoleBranch entity = SecRoleBranch.builder()
                .roleIdFk(request.getRoleIdFk())
                .branchIdFk(request.getBranchIdFk())
                .dataAccessLevel(request.getDataAccessLevel())
                .build();

        SecRoleBranch saved = repo.save(entity);
        log.info("Created SEC_ROLE_BRANCH for role {} branch {}", saved.getRoleIdFk(), saved.getBranchIdFk());
        return ServiceResult.success(SecRoleBranchMapper.toDto(saved), Status.CREATED);
    }

    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).ROLE_UPDATE)")
    @Transactional
    public ServiceResult<SecRoleBranchDto> update(Long roleId, Long branchId, UpdateSecRoleBranchRequest request) {
        log.info("Updating SEC_ROLE_BRANCH for role {} branch {}", roleId, branchId);

        SecRoleBranch entity = repo.findById(new SecRoleBranchId(roleId, branchId))
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_ROLE_BRANCH_NOT_FOUND, roleId, branchId));

        // RULE-SEC-035 — data access level required + validated against LOV-SEC-002
        assertValidDataAccessLevel(request.getDataAccessLevel());
        entity.setDataAccessLevel(request.getDataAccessLevel());

        SecRoleBranch saved = repo.save(entity);
        log.info("Updated SEC_ROLE_BRANCH for role {} branch {}", saved.getRoleIdFk(), saved.getBranchIdFk());
        return ServiceResult.success(SecRoleBranchMapper.toDto(saved), Status.UPDATED);
    }

    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).ROLE_DELETE)")
    @Transactional
    public void delete(Long roleId, Long branchId) {
        log.info("Deleting SEC_ROLE_BRANCH for role {} branch {}", roleId, branchId);
        SecRoleBranch entity = repo.findById(new SecRoleBranchId(roleId, branchId))
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_ROLE_BRANCH_NOT_FOUND, roleId, branchId));
        repo.delete(entity);
        log.info("Deleted SEC_ROLE_BRANCH for role {} branch {}", roleId, branchId);
    }

    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).ROLE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<SecRoleBranchDto> getById(Long roleId, Long branchId) {
        log.debug("Fetching SEC_ROLE_BRANCH for role {} branch {}", roleId, branchId);
        SecRoleBranch entity = repo.findById(new SecRoleBranchId(roleId, branchId))
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_ROLE_BRANCH_NOT_FOUND, roleId, branchId));
        return ServiceResult.success(SecRoleBranchMapper.toDto(entity));
    }

    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).ROLE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<Page<SecRoleBranchDto>> listRoleBranches(Pageable pageable) {
        log.debug("Listing SEC_ROLE_BRANCH records");
        Pageable validated = PageableValidator.validateSortFields(pageable, ALLOWED_SORT_FIELDS);
        Page<SecRoleBranch> page = repo.findAll(validated);
        return ServiceResult.success(page.map(SecRoleBranchMapper::toDto));
    }

    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).ROLE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<Page<SecRoleBranchDto>> search(SearchRequest request) {
        log.debug("Searching SEC_ROLE_BRANCH records");
        Specification<SecRoleBranch> spec = SpecBuilder.build(
                request, new SetAllowedFields(ALLOWED_SEARCH_FIELDS), DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(request, ALLOWED_SORT_FIELDS, "roleIdFk");

        Page<SecRoleBranch> page = (spec != null) ? repo.findAll(spec, pageable) : repo.findAll(pageable);
        return ServiceResult.success(page.map(SecRoleBranchMapper::toDto));
    }

    private void assertValidDataAccessLevel(String dataAccessLevel) {
        if (dataAccessLevel == null || dataAccessLevel.isBlank()) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED);
        }
        lookupClient.assertValidDataAccessLevel(dataAccessLevel);
    }
}
