package com.example.org.service;

import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.org.domain.OrgLegalEntity;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgLegalEntityMapper;
import com.example.org.numbering.OrgCodeGenerator;
import com.example.org.repository.OrgLegalEntityRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgLegalEntityService {

    private final OrgLegalEntityRepository repository;
    private final OrgLegalEntityMapper mapper;
    private final OrgCodeGenerator codeGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "legalEntityCode", "nameAr", "nameEn", "entityTypeId", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_CREATE')")
    public ServiceResult<LegalEntityResponse> create(LegalEntityCreateRequest request) {
        log.info("Creating LegalEntity nameEn={}", request.getNameEn());

        if (repository.existsByNameArIgnoreCase(request.getNameAr())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCase(request.getNameEn())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        OrgLegalEntity entity = mapper.toEntity(request);
        entity.setLegalEntityCode(codeGenerator.generateLegalEntityCode());

        OrgLegalEntity saved = repository.save(entity);
        log.info("Created LegalEntity id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_UPDATE')")
    public ServiceResult<LegalEntityResponse> update(Long id, LegalEntityUpdateRequest request) {
        log.info("Updating LegalEntity id={}", id);

        OrgLegalEntity entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        if (repository.existsByNameArIgnoreCaseAndIdNot(request.getNameAr(), id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndIdNot(request.getNameEn(), id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        mapper.updateEntityFromRequest(entity, request);
        OrgLegalEntity saved = repository.save(entity);
        log.info("Updated LegalEntity id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_VIEW')")
    public ServiceResult<LegalEntityResponse> getById(Long id) {
        log.debug("Fetching LegalEntity id={}", id);
        OrgLegalEntity entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_VIEW')")
    public ServiceResult<Page<LegalEntityResponse>> search(LegalEntitySearchRequest searchRequest) {
        log.debug("Searching LegalEntities");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgLegalEntity> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgLegalEntity> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_UPDATE')")
    public ServiceResult<LegalEntityResponse> activate(Long id) {
        log.info("Activating LegalEntity id={}", id);
        OrgLegalEntity entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        OrgLegalEntity saved = repository.save(entity);
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_UPDATE')")
    public ServiceResult<LegalEntityResponse> deactivate(Long id) {
        log.info("Deactivating LegalEntity id={}", id);
        OrgLegalEntity entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeBranches = repository.countActiveBranches(id);
        if (activeBranches > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.LE_HAS_ACTIVE_BRANCHES, id);
        }
        long activeProfitCenters = repository.countActiveProfitCenters(id);
        if (activeProfitCenters > 0) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.LE_HAS_ACTIVE_PROFIT_CENTERS, id);
        }

        entity.deactivate();
        OrgLegalEntity saved = repository.save(entity);
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_DELETE')")
    public void delete(Long id) {
        log.info("Deleting LegalEntity id={}", id);
        OrgLegalEntity entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        long activeBranches = repository.countActiveBranches(id);
        if (activeBranches > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.LE_HAS_ACTIVE_BRANCHES, id);
        }
        long activeProfitCenters = repository.countActiveProfitCenters(id);
        if (activeProfitCenters > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.LE_HAS_ACTIVE_PROFIT_CENTERS, id);
        }

        repository.delete(entity);
        log.info("Deleted LegalEntity id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_VIEW')")
    public ServiceResult<LegalEntityUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for LegalEntity id={}", id);
        OrgLegalEntity entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        long activeBranches = repository.countActiveBranches(id);
        long activeProfitCenters = repository.countActiveProfitCenters(id);
        return ServiceResult.success(mapper.toUsageResponse(entity, activeBranches, activeProfitCenters));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_LEGAL_ENTITY_VIEW')")
    public ServiceResult<List<LegalEntityOptionResponse>> listOptions() {
        log.debug("Listing LegalEntity options");
        List<LegalEntityOptionResponse> options = repository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsActiveFl()))
                .map(mapper::toOptionResponse)
                .toList();
        return ServiceResult.success(options);
    }
}
