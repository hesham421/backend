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
import com.erp.org.crossmodule.OrgBranchApi;
import com.erp.org.crossmodule.OrgBranchView;
import com.erp.security.dto.CreateSecUserProfileRequest;
import com.erp.security.dto.SecUserProfileDto;
import com.erp.security.dto.UpdateSecUserProfileRequest;
import com.erp.security.entity.SecUserProfile;
import com.erp.security.entity.UserAccount;
import com.erp.security.exception.SecurityErrorCodes;
import com.erp.security.mapper.SecUserProfileMapper;
import com.erp.security.repository.SecUserProfileRepository;
import com.erp.security.repository.UserAccountRepository;
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
 * Service for SEC_USER_PROFILE CRUD (API-SEC-032..035, execution-plan-SEC-gaps.md Phase SVC+API).
 *
 * {@code @PreAuthorize} per Phase SEC (Section 8.1 Permissions Matrix) — no DELETE method
 * exists (profiles deactivate via isActiveFl through UPDATE, never DELETE).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecUserProfileService {

    private final SecUserProfileRepository repo;
    private final UserAccountRepository userAccountRepo;
    private final OrgBranchApi orgBranchApi;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "userIdFk", "branchIdFk", "isActiveFl", "createdAt"
    );
    // fullNameAr/fullNameEn — OQ-010: search-only (not sortable), matching the frontend
    // grid config's sortable: false for these columns.
    private static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of(
            "userIdFk", "branchIdFk", "isActiveFl", "createdAt", "fullNameAr", "fullNameEn"
    );

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).USER_PROFILE_CREATE)")
    @Transactional
    public ServiceResult<SecUserProfileDto> create(CreateSecUserProfileRequest request) {
        log.info("Creating SEC_USER_PROFILE for user ID: {}", request.getUserIdFk());

        if (repo.existsById(request.getUserIdFk())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.SEC_USER_PROFILE_ALREADY_EXISTS, request.getUserIdFk());
        }
        UserAccount user = userAccountRepo.findById(request.getUserIdFk())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.USER_NOT_FOUND, request.getUserIdFk()));

        // RULE-SEC-034 — reject if the referenced branch is not active
        assertActiveBranch(request.getBranchIdFk());

        SecUserProfile entity = SecUserProfile.builder()
                .userIdFk(user.getId())
                .user(user)
                .branchIdFk(request.getBranchIdFk())
                .fullNameAr(request.getFullNameAr())
                .fullNameEn(request.getFullNameEn())
                .preferredLang(request.getPreferredLang())
                .employeeIdFk(request.getEmployeeIdFk())
                .build();

        SecUserProfile saved = repo.save(entity);
        log.info("Created SEC_USER_PROFILE for user ID: {}", saved.getUserIdFk());
        return ServiceResult.success(SecUserProfileMapper.toDto(saved), Status.CREATED);
    }

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).USER_PROFILE_UPDATE)")
    @Transactional
    public ServiceResult<SecUserProfileDto> update(Long userId, UpdateSecUserProfileRequest request) {
        log.info("Updating SEC_USER_PROFILE for user ID: {}", userId);

        SecUserProfile entity = repo.findById(userId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_USER_PROFILE_NOT_FOUND, userId));

        // RULE-SEC-034 — reject if the referenced branch is not active
        assertActiveBranch(request.getBranchIdFk());

        entity.setBranchIdFk(request.getBranchIdFk());
        entity.setFullNameAr(request.getFullNameAr());
        entity.setFullNameEn(request.getFullNameEn());
        entity.setPreferredLang(request.getPreferredLang());
        entity.setEmployeeIdFk(request.getEmployeeIdFk());

        SecUserProfile saved = repo.save(entity);
        log.info("Updated SEC_USER_PROFILE for user ID: {}", saved.getUserIdFk());
        return ServiceResult.success(SecUserProfileMapper.toDto(saved), Status.UPDATED);
    }

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).USER_PROFILE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<SecUserProfileDto> getById(Long userId) {
        log.debug("Fetching SEC_USER_PROFILE for user ID: {}", userId);
        SecUserProfile entity = repo.findById(userId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_USER_PROFILE_NOT_FOUND, userId));
        return ServiceResult.success(SecUserProfileMapper.toDto(entity));
    }

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).USER_PROFILE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<Page<SecUserProfileDto>> listProfiles(Pageable pageable) {
        log.debug("Listing SEC_USER_PROFILE records");
        Pageable validated = PageableValidator.validateSortFields(pageable, ALLOWED_SORT_FIELDS);
        Page<SecUserProfile> page = repo.findAll(validated);
        return ServiceResult.success(page.map(SecUserProfileMapper::toDto));
    }

    @PreAuthorize("hasAuthority(T(com.erp.security.constants.SecurityPermissions).USER_PROFILE_VIEW)")
    @Transactional(readOnly = true)
    public ServiceResult<Page<SecUserProfileDto>> search(SearchRequest request) {
        log.debug("Searching SEC_USER_PROFILE records");
        Specification<SecUserProfile> spec = SpecBuilder.build(
                request, new SetAllowedFields(ALLOWED_SEARCH_FIELDS), DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(request, ALLOWED_SORT_FIELDS, "userIdFk");

        Page<SecUserProfile> page = (spec != null) ? repo.findAll(spec, pageable) : repo.findAll(pageable);
        return ServiceResult.success(page.map(SecUserProfileMapper::toDto));
    }

    /**
     * RULE-SEC-034 — reject if the referenced ORG_BRANCH does not exist or is not active.
     * Throws LocalizedException(ERR-SEC-1034) in both cases — the not-found/not-active
     * decision itself stays here (this Service's own business decision); {@link OrgBranchApi}
     * only supplies the raw branch data, per the Domain Delegation split in
     * create-service's SKILL.md.
     */
    private void assertActiveBranch(Long branchId) {
        OrgBranchView branch = orgBranchApi.findBranch(branchId).orElse(null);
        if (branch == null || !branch.active()) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.SEC_USER_PROFILE_BRANCH_INACTIVE, branchId);
        }
    }
}
