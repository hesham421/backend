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
import com.example.erp.org.domain.OrgLegalEntityDomain;
import com.example.erp.org.dto.LegalEntityCreateRequest;
import com.example.erp.org.dto.LegalEntityResponse;
import com.example.erp.org.dto.LegalEntitySearchRequest;
import com.example.erp.org.dto.LegalEntityUpdateRequest;
import com.example.erp.org.entity.OrgLegalEntity;
import com.example.erp.org.exception.OrgErrorCodes;
import com.example.erp.org.mapper.LegalEntityMapper;
import com.example.erp.org.repository.BranchRepository;
import com.example.erp.org.repository.LegalEntityRepository;
import com.example.erp.org.repository.ProfitCenterRepository;
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
 * Orchestration for LegalEntity (API-ORG-001..006). Business Rule decisions (RULE-ORG-001/002
 * deactivation guards) are delegated to {@link OrgLegalEntityDomain} — see domain-layer.md.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LegalEntityService {

    private final LegalEntityRepository legalEntityRepository;
    private final BranchRepository branchRepository;
    private final ProfitCenterRepository profitCenterRepository;
    private final LegalEntityMapper legalEntityMapper;
    private final OrgNumberGenerator orgNumberGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "legalEntityCode", "nameAr", "nameEn", "entityTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LEGAL_ENTITY_CREATE)")
    public ServiceResult<LegalEntityResponse> create(LegalEntityCreateRequest request) {
        log.info("Creating LegalEntity with nameEn: {}", request.getNameEn());

        assertNameNotDuplicate(request.getNameAr(), request.getNameEn(), null);

        String generatedCode = orgNumberGenerator.next(
            "LE-", legalEntityRepository.count(), legalEntityRepository::existsByLegalEntityCode);
        OrgLegalEntityDomain.create(generatedCode);

        OrgLegalEntity entity = legalEntityMapper.toEntity(request, generatedCode);
        OrgLegalEntity saved = legalEntityRepository.save(entity);

        log.info("Created LegalEntity ID: {}, code: {}", saved.getId(), saved.getLegalEntityCode());
        return ServiceResult.success(legalEntityMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LEGAL_ENTITY_UPDATE)")
    public ServiceResult<LegalEntityResponse> update(Long id, LegalEntityUpdateRequest request) {
        log.info("Updating LegalEntity ID: {}", id);

        OrgLegalEntity entity = findOrThrow(id);
        assertNameNotDuplicate(request.getNameAr(), request.getNameEn(), id);

        legalEntityMapper.updateEntityFromRequest(entity, request);
        OrgLegalEntity saved = legalEntityRepository.save(entity);

        log.info("Updated LegalEntity ID: {}", saved.getId());
        return ServiceResult.success(legalEntityMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LEGAL_ENTITY_VIEW)")
    public ServiceResult<LegalEntityResponse> getById(Long id) {
        log.debug("Fetching LegalEntity ID: {}", id);
        return ServiceResult.success(legalEntityMapper.toResponse(findOrThrow(id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LEGAL_ENTITY_VIEW)")
    public ServiceResult<Page<LegalEntityResponse>> search(LegalEntitySearchRequest searchRequest) {
        log.debug("Searching LegalEntity");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        AllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgLegalEntity> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<OrgLegalEntity> page = legalEntityRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(legalEntityMapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LEGAL_ENTITY_UPDATE)")
    public ServiceResult<LegalEntityResponse> activate(Long id) {
        log.info("Activating LegalEntity ID: {}", id);

        OrgLegalEntity entity = findOrThrow(id);
        entity.activate();
        OrgLegalEntity saved = legalEntityRepository.save(entity);

        log.info("Activated LegalEntity ID: {}", saved.getId());
        return ServiceResult.success(legalEntityMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).LEGAL_ENTITY_UPDATE)")
    public ServiceResult<LegalEntityResponse> deactivate(Long id) {
        log.info("Deactivating LegalEntity ID: {}", id);

        OrgLegalEntity entity = findOrThrow(id);

        long activeBranchCount = branchRepository.countByLegalEntity_IdAndIsActiveFlTrue(id);
        long activeProfitCenterCount = profitCenterRepository.countByLegalEntity_IdAndIsActiveFlTrue(id);
        OrgLegalEntityDomain.from(entity).assertCanDeactivate(activeBranchCount, activeProfitCenterCount);

        entity.deactivate();
        OrgLegalEntity saved = legalEntityRepository.save(entity);

        log.info("Deactivated LegalEntity ID: {}", saved.getId());
        return ServiceResult.success(legalEntityMapper.toResponse(saved), Status.UPDATED);
    }

    private OrgLegalEntity findOrThrow(Long id) {
        return legalEntityRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "LegalEntity", id));
    }

    /** RULE-ORG-015 — name uniqueness, global scope for LegalEntity. {@code excludeId} null on create. */
    private void assertNameNotDuplicate(String nameAr, String nameEn, Long excludeId) {
        if (nameAr != null) {
            boolean taken = excludeId == null
                ? legalEntityRepository.existsByNameAr(nameAr)
                : legalEntityRepository.existsByNameArAndIdNot(nameAr, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameAr);
            }
        }
        if (nameEn != null) {
            boolean taken = excludeId == null
                ? legalEntityRepository.existsByNameEn(nameEn)
                : legalEntityRepository.existsByNameEnAndIdNot(nameEn, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameEn);
            }
        }
    }
}
