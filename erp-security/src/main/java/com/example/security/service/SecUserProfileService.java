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
import com.example.security.client.OrgBranchClient;
import com.example.security.dto.CreateSecUserProfileRequest;
import com.example.security.dto.SecUserProfileDto;
import com.example.security.dto.UpdateSecUserProfileRequest;
import com.example.security.entity.SecUserProfile;
import com.example.security.entity.UserAccount;
import com.example.security.exception.SecurityErrorCodes;
import com.example.security.mapper.SecUserProfileMapper;
import com.example.security.repository.SecUserProfileRepository;
import com.example.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service for SEC_USER_PROFILE CRUD (API-SEC-032..035, execution-plan-SEC-gaps.md Phase SVC+API).
 *
 * NOTE: no {@code @PreAuthorize} on these methods yet — Phase SEC (Section 8.1 Permissions
 * Matrix) owns adding permission gates for the DataScope endpoints; this phase only needs the
 * endpoints to exist and enforce their bound RULE-IDs (per 03-PHASE-SVC-API.md Definition of
 * Done, item 5). Endpoints remain behind the standard JWT filter (authenticated, not public).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecUserProfileService {

    private final SecUserProfileRepository repo;
    private final UserAccountRepository userAccountRepo;
    private final OrgBranchClient orgBranchClient;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "userIdFk", "branchIdFk", "isActiveFl", "createdAt"
    );
    private static final Set<String> ALLOWED_SEARCH_FIELDS = ALLOWED_SORT_FIELDS;

    @Transactional
    public ServiceResult<SecUserProfileDto> create(CreateSecUserProfileRequest request) {
        log.info("Creating SEC_USER_PROFILE for user ID: {}", request.getUserIdFk());

        if (repo.existsById(request.getUserIdFk())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.SEC_USER_PROFILE_ALREADY_EXISTS, request.getUserIdFk());
        }
        UserAccount user = userAccountRepo.findById(request.getUserIdFk())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.USER_NOT_FOUND, request.getUserIdFk()));

        // RULE-SEC-034 — reject if the referenced branch is not active
        orgBranchClient.assertActiveBranch(request.getBranchIdFk());

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

    @Transactional
    public ServiceResult<SecUserProfileDto> update(Long userId, UpdateSecUserProfileRequest request) {
        log.info("Updating SEC_USER_PROFILE for user ID: {}", userId);

        SecUserProfile entity = repo.findById(userId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_USER_PROFILE_NOT_FOUND, userId));

        // RULE-SEC-034 — reject if the referenced branch is not active
        orgBranchClient.assertActiveBranch(request.getBranchIdFk());

        entity.setBranchIdFk(request.getBranchIdFk());
        entity.setFullNameAr(request.getFullNameAr());
        entity.setFullNameEn(request.getFullNameEn());
        entity.setPreferredLang(request.getPreferredLang());
        entity.setEmployeeIdFk(request.getEmployeeIdFk());

        SecUserProfile saved = repo.save(entity);
        log.info("Updated SEC_USER_PROFILE for user ID: {}", saved.getUserIdFk());
        return ServiceResult.success(SecUserProfileMapper.toDto(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    public ServiceResult<SecUserProfileDto> getById(Long userId) {
        log.debug("Fetching SEC_USER_PROFILE for user ID: {}", userId);
        SecUserProfile entity = repo.findById(userId)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.SEC_USER_PROFILE_NOT_FOUND, userId));
        return ServiceResult.success(SecUserProfileMapper.toDto(entity));
    }

    @Transactional(readOnly = true)
    public ServiceResult<Page<SecUserProfileDto>> listProfiles(Pageable pageable) {
        log.debug("Listing SEC_USER_PROFILE records");
        Pageable validated = PageableValidator.validateSortFields(pageable, ALLOWED_SORT_FIELDS);
        Page<SecUserProfile> page = repo.findAll(validated);
        return ServiceResult.success(page.map(SecUserProfileMapper::toDto));
    }

    @Transactional(readOnly = true)
    public ServiceResult<Page<SecUserProfileDto>> search(SearchRequest request) {
        log.debug("Searching SEC_USER_PROFILE records");
        Specification<SecUserProfile> spec = SpecBuilder.build(
                request, new SetAllowedFields(ALLOWED_SEARCH_FIELDS), DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(request, ALLOWED_SORT_FIELDS, "userIdFk");

        Page<SecUserProfile> page = (spec != null) ? repo.findAll(spec, pageable) : repo.findAll(pageable);
        return ServiceResult.success(page.map(SecUserProfileMapper::toDto));
    }
}
