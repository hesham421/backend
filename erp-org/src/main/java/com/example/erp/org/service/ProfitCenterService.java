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
import com.example.erp.org.dto.ProfitCenterCreateRequest;
import com.example.erp.org.dto.ProfitCenterResponse;
import com.example.erp.org.dto.ProfitCenterSearchRequest;
import com.example.erp.org.dto.ProfitCenterUpdateRequest;
import com.example.erp.org.entity.OrgLegalEntity;
import com.example.erp.org.entity.OrgProfitCenter;
import com.example.erp.org.exception.OrgErrorCodes;
import com.example.erp.org.mapper.ProfitCenterMapper;
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
 * Orchestration for ProfitCenter (API-ORG-033..038). No {@code OrgProfitCenterDomain} exists —
 * per its entity Javadoc, no RULE-ID for this entity answers "is this operation allowed?" (no
 * parent-active create guard, no internal deactivation guard). Only the standard
 * RULE-ORG-011..016 code/name/audit set applies, satisfied here and at the DTO/Repository layers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfitCenterService {

    private final ProfitCenterRepository profitCenterRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final ProfitCenterMapper profitCenterMapper;
    private final OrgNumberGenerator orgNumberGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "profitCenterCode", "nameAr", "nameEn", "legalEntity.id", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).PROFIT_CENTER_CREATE)")
    public ServiceResult<ProfitCenterResponse> create(ProfitCenterCreateRequest request) {
        log.info("Creating ProfitCenter under LegalEntity ID: {}", request.getLegalEntityFk());

        OrgLegalEntity parent = legalEntityRepository.findById(request.getLegalEntityFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "LegalEntity", request.getLegalEntityFk()));

        assertNameNotDuplicate(parent.getId(), request.getNameAr(), request.getNameEn(), null);

        String generatedCode = orgNumberGenerator.next(
            "PC-" + parent.getLegalEntityCode() + "-",
            profitCenterRepository.countByLegalEntity_Id(parent.getId()),
            code -> profitCenterRepository.existsByLegalEntity_IdAndProfitCenterCode(parent.getId(), code));

        OrgProfitCenter entity = profitCenterMapper.toEntity(request, generatedCode, parent);
        OrgProfitCenter saved = profitCenterRepository.save(entity);

        log.info("Created ProfitCenter ID: {}, code: {}", saved.getId(), saved.getProfitCenterCode());
        return ServiceResult.success(profitCenterMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).PROFIT_CENTER_UPDATE)")
    public ServiceResult<ProfitCenterResponse> update(Long id, ProfitCenterUpdateRequest request) {
        log.info("Updating ProfitCenter ID: {}", id);

        OrgProfitCenter entity = findOrThrow(id);
        assertNameNotDuplicate(entity.getLegalEntity().getId(), request.getNameAr(), request.getNameEn(), id);

        profitCenterMapper.updateEntityFromRequest(entity, request);
        OrgProfitCenter saved = profitCenterRepository.save(entity);

        log.info("Updated ProfitCenter ID: {}", saved.getId());
        return ServiceResult.success(profitCenterMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).PROFIT_CENTER_VIEW)")
    public ServiceResult<ProfitCenterResponse> getById(Long id) {
        log.debug("Fetching ProfitCenter ID: {}", id);
        return ServiceResult.success(profitCenterMapper.toResponse(findOrThrow(id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).PROFIT_CENTER_VIEW)")
    public ServiceResult<Page<ProfitCenterResponse>> search(ProfitCenterSearchRequest searchRequest) {
        log.debug("Searching ProfitCenter");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        AllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgProfitCenter> spec =
            SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

        Page<OrgProfitCenter> page = profitCenterRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(profitCenterMapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).PROFIT_CENTER_UPDATE)")
    public ServiceResult<ProfitCenterResponse> activate(Long id) {
        log.info("Activating ProfitCenter ID: {}", id);

        OrgProfitCenter entity = findOrThrow(id);
        entity.activate();
        OrgProfitCenter saved = profitCenterRepository.save(entity);

        log.info("Activated ProfitCenter ID: {}", saved.getId());
        return ServiceResult.success(profitCenterMapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).PROFIT_CENTER_UPDATE)")
    public ServiceResult<ProfitCenterResponse> deactivate(Long id) {
        log.info("Deactivating ProfitCenter ID: {}", id);

        // No entity-specific deactivation guard for ProfitCenter (per SRS A6 lifecycle table).
        OrgProfitCenter entity = findOrThrow(id);
        entity.deactivate();
        OrgProfitCenter saved = profitCenterRepository.save(entity);

        log.info("Deactivated ProfitCenter ID: {}", saved.getId());
        return ServiceResult.success(profitCenterMapper.toResponse(saved), Status.UPDATED);
    }

    private OrgProfitCenter findOrThrow(Long id) {
        return profitCenterRepository.findById(id)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, OrgErrorCodes.RECORD_NOT_FOUND, "ProfitCenter", id));
    }

    /** RULE-ORG-015 — name uniqueness within the parent LegalEntity scope. {@code excludeId} null on create. */
    private void assertNameNotDuplicate(Long legalEntityId, String nameAr, String nameEn, Long excludeId) {
        if (nameAr != null) {
            boolean taken = excludeId == null
                ? profitCenterRepository.existsByLegalEntity_IdAndNameAr(legalEntityId, nameAr)
                : profitCenterRepository.existsByLegalEntity_IdAndNameArAndIdNot(legalEntityId, nameAr, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameAr);
            }
        }
        if (nameEn != null) {
            boolean taken = excludeId == null
                ? profitCenterRepository.existsByLegalEntity_IdAndNameEn(legalEntityId, nameEn)
                : profitCenterRepository.existsByLegalEntity_IdAndNameEnAndIdNot(legalEntityId, nameEn, excludeId);
            if (taken) {
                throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, nameEn);
            }
        }
    }
}
