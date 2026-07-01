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
import com.example.org.domain.OrgProfitCenter;
import com.example.org.dto.*;
import com.example.org.exception.OrgErrorCodes;
import com.example.org.mapper.OrgProfitCenterMapper;
import com.example.org.numbering.OrgCodeGenerator;
import com.example.org.repository.OrgLegalEntityRepository;
import com.example.org.repository.OrgProfitCenterRepository;
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
public class OrgProfitCenterService {

    private final OrgProfitCenterRepository repository;
    private final OrgLegalEntityRepository legalEntityRepository;
    private final OrgProfitCenterMapper mapper;
    private final OrgCodeGenerator codeGenerator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "profitCenterCode", "nameAr", "nameEn", "isActiveFl", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_CREATE')")
    public ServiceResult<ProfitCenterResponse> create(ProfitCenterCreateRequest request) {
        log.info("Creating ProfitCenter nameEn={} under legalEntityId={}", request.getNameEn(), request.getLegalEntityId());

        OrgLegalEntity legalEntity = legalEntityRepository.findById(request.getLegalEntityId())
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, request.getLegalEntityId()));
        if (!Boolean.TRUE.equals(legalEntity.getIsActiveFl())) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, OrgErrorCodes.INACTIVE_LEGAL_ENTITY, request.getLegalEntityId());
        }

        if (repository.existsByNameArIgnoreCaseAndLegalEntityId(request.getNameAr(), request.getLegalEntityId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndLegalEntityId(request.getNameEn(), request.getLegalEntityId())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        OrgProfitCenter entity = mapper.toEntity(request, legalEntity);
        entity.setProfitCenterCode(codeGenerator.generateProfitCenterCode(legalEntity.getLegalEntityCode()));

        OrgProfitCenter saved = repository.save(entity);
        log.info("Created ProfitCenter id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_UPDATE')")
    public ServiceResult<ProfitCenterResponse> update(Long id, ProfitCenterUpdateRequest request) {
        log.info("Updating ProfitCenter id={}", id);

        OrgProfitCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));

        Long leId = entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null;
        if (repository.existsByNameArIgnoreCaseAndLegalEntityIdAndIdNot(request.getNameAr(), leId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameAr());
        }
        if (repository.existsByNameEnIgnoreCaseAndLegalEntityIdAndIdNot(request.getNameEn(), leId, id)) {
            throw new LocalizedException(Status.ALREADY_EXISTS, OrgErrorCodes.NAME_DUPLICATE, request.getNameEn());
        }

        mapper.updateEntityFromRequest(entity, request);
        OrgProfitCenter saved = repository.save(entity);
        log.info("Updated ProfitCenter id={}", saved.getId());
        return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_VIEW')")
    public ServiceResult<ProfitCenterResponse> getById(Long id) {
        log.debug("Fetching ProfitCenter id={}", id);
        OrgProfitCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_VIEW')")
    public ServiceResult<Page<ProfitCenterResponse>> search(ProfitCenterSearchRequest searchRequest) {
        log.debug("Searching ProfitCenters");
        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<OrgProfitCenter> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);
        Page<OrgProfitCenter> page = repository.findAll(spec, pageable);
        return ServiceResult.success(page.map(mapper::toResponse));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_UPDATE')")
    public ServiceResult<ProfitCenterResponse> activate(Long id) {
        log.info("Activating ProfitCenter id={}", id);
        OrgProfitCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.activate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_UPDATE')")
    public ServiceResult<ProfitCenterResponse> deactivate(Long id) {
        log.info("Deactivating ProfitCenter id={}", id);
        OrgProfitCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        entity.deactivate();
        return ServiceResult.success(mapper.toResponse(repository.save(entity)), Status.UPDATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_DELETE')")
    public void delete(Long id) {
        log.info("Deleting ProfitCenter id={}", id);
        OrgProfitCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        repository.delete(entity);
        log.info("Deleted ProfitCenter id={}", id);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_VIEW')")
    public ServiceResult<ProfitCenterUsageResponse> getUsage(Long id) {
        log.debug("Fetching usage for ProfitCenter id={}", id);
        OrgProfitCenter entity = repository.findById(id)
                .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, OrgErrorCodes.NOT_FOUND, id));
        return ServiceResult.success(mapper.toUsageResponse(entity));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_PROFIT_CENTER_VIEW')")
    public ServiceResult<List<ProfitCenterOptionResponse>> listOptions(Long legalEntityId) {
        log.debug("Listing ProfitCenter options for legalEntityId={}", legalEntityId);
        List<ProfitCenterOptionResponse> options = repository.findAll().stream()
                .filter(pc -> Boolean.TRUE.equals(pc.getIsActiveFl())
                        && (legalEntityId == null || (pc.getLegalEntity() != null && legalEntityId.equals(pc.getLegalEntity().getId()))))
                .map(mapper::toOptionResponse)
                .toList();
        return ServiceResult.success(options);
    }
}
