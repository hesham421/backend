package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.web.util.PageableValidator;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.erp.masterdata.crossmodule.LookupValueView;
import com.erp.masterdata.crossmodule.MasterDataLookupApi;
import com.erp.security.dto.CreateSecRoleBranchRequest;
import com.erp.security.dto.SecRoleBranchDto;
import com.erp.security.dto.UpdateSecRoleBranchRequest;
import com.erp.security.entity.SecRoleBranch;
import com.erp.security.entity.SecRoleBranchId;
import com.erp.security.exception.SecurityErrorCodes;
import com.erp.security.mapper.SecRoleBranchMapper;
import com.erp.security.repository.RoleRepository;
import com.erp.security.repository.SecRoleBranchRepository;
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
    private final MasterDataLookupApi masterDataLookupApi;

    private static final String DATA_ACCESS_LEVEL_LOOKUP_CODE = "DATA_ACCESS_LEVEL";

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "roleIdFk", "branchIdFk", "dataAccessLevel", "isActiveFl", "createdAt"
    );
    private static final Set<String> ALLOWED_SEARCH_FIELDS = ALLOWED_SORT_FIELDS;

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_CREATE)")
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

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_UPDATE)")
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

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_DELETE)")
    @Transactional
    public void delete(Long roleId, Long branchId) {
        log.info("Deleting SEC_ROLE_BRANCH for role {} branch {}", roleId, branchId);
        SecRoleBranch entity = repo.findById(new SecRoleBranchId(roleId, branchId))
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_ROLE_BRANCH_NOT_FOUND, roleId, branchId));
        repo.delete(entity);
        log.info("Deleted SEC_ROLE_BRANCH for role {} branch {}", roleId, branchId);
    }

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<SecRoleBranchDto> getById(Long roleId, Long branchId) {
        log.debug("Fetching SEC_ROLE_BRANCH for role {} branch {}", roleId, branchId);
        SecRoleBranch entity = repo.findById(new SecRoleBranchId(roleId, branchId))
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_ROLE_BRANCH_NOT_FOUND, roleId, branchId));
        return ServiceResult.success(SecRoleBranchMapper.toDto(entity));
    }

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<Page<SecRoleBranchDto>> listRoleBranches(Pageable pageable) {
        log.debug("Listing SEC_ROLE_BRANCH records");
        Pageable validated = PageableValidator.validateSortFields(pageable, ALLOWED_SORT_FIELDS);
        Page<SecRoleBranch> page = repo.findAll(validated);
        return ServiceResult.success(page.map(SecRoleBranchMapper::toDto));
    }

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).ROLE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<Page<SecRoleBranchDto>> search(SearchRequest request) {
        log.debug("Searching SEC_ROLE_BRANCH records");
        Specification<SecRoleBranch> spec = SpecBuilder.build(
                request, new SetAllowedFields(ALLOWED_SEARCH_FIELDS), DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(request, ALLOWED_SORT_FIELDS, "roleIdFk");

        Page<SecRoleBranch> page = (spec != null) ? repo.findAll(spec, pageable) : repo.findAll(pageable);
        return ServiceResult.success(page.map(SecRoleBranchMapper::toDto));
    }

    /**
     * RULE-SEC-035 — dataAccessLevel must be one of the active LOV-SEC-002 codes
     * (BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL). Throws LocalizedException(ERR-SEC-1035)
     * if not — the plan binds exactly one ERR-ID to RULE-SEC-035, covering both the
     * "missing" and "not a valid LOV code" scenarios. The validity check itself stays here
     * (this Service's own business decision) — {@link MasterDataLookupApi} only supplies the
     * raw active-values data, per the Domain Delegation split in create-service's SKILL.md.
     */
    private void assertValidDataAccessLevel(String dataAccessLevel) {
        if (dataAccessLevel == null || dataAccessLevel.isBlank()) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED);
        }
        List<LookupValueView> values = masterDataLookupApi.getActiveValues(DATA_ACCESS_LEVEL_LOOKUP_CODE);
        boolean valid = values.stream()
                .anyMatch(v -> v.code() != null && v.code().equalsIgnoreCase(dataAccessLevel));
        if (!valid) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED, dataAccessLevel);
        }
    }
}
