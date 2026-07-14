package com.example.erp.notification.service;

import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.notification.dto.NotificationTemplateCreateRequest;
import com.example.erp.notification.dto.NotificationTemplateResponse;
import com.example.erp.notification.dto.NotificationTemplateSearchRequest;
import com.example.erp.notification.dto.NotificationTemplateUpdateRequest;
import com.example.erp.notification.entity.NotificationTemplate;
import com.example.erp.notification.exception.NotificationErrorCodes;
import com.example.erp.notification.mapper.NotificationTemplateMapper;
import com.example.erp.notification.repository.NotificationTemplateRepository;
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
 * API-NOTIF-006 (Search), 007 (Create), 008 (Update), 009 (Deactivate), 010 (Get by ID).
 * No {@code <Entity>Domain} object — CORE.md declares this module's domain behavior embedded in
 * entity methods; RULE-NOTIF-006/007 here are completeness/uniqueness guards (structural,
 * per domain-layer.md's Decision Test), not "is this operation allowed?" invariants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    // QR-NOTIF-007 ALLOWED_SORT_FIELDS, per SVCAPI.md
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "templateCode", "templateNameAr", "templateNameEn", "createdAt");

    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateMapper templateMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_TEMPLATE_VIEW)")
    public ServiceResult<Page<NotificationTemplateResponse>> search(NotificationTemplateSearchRequest searchRequest) {
        log.debug("Searching notification templates");

        SearchRequest commonRequest = searchRequest.toCommonSearchRequest();
        SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
        Specification<NotificationTemplate> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS, "templateCode");

        Page<NotificationTemplate> page = templateRepository.findAll(spec, pageable);
        return ServiceResult.success(page.map(templateMapper::toResponse));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_TEMPLATE_VIEW)")
    public ServiceResult<NotificationTemplateResponse> getById(Long id) {
        log.debug("Fetching NotificationTemplate ID: {}", id);
        NotificationTemplate entity = findOrThrow(id);
        return ServiceResult.success(templateMapper.toResponse(entity));
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_TEMPLATE_CREATE)")
    public ServiceResult<NotificationTemplateResponse> create(NotificationTemplateCreateRequest request) {
        log.info("Creating NotificationTemplate with code: {}", request.getTemplateCode());

        assertBilingual(request.getTemplateBodyAr(), request.getTemplateBodyEn());

        // RULE-NOTIF-007 — uniqueness pre-check (also DB-enforced via UQ_NOTIF_TEMPLATE_CODE)
        if (templateRepository.existsByTemplateCode(request.getTemplateCode())) {
            throw new LocalizedException(Status.ALREADY_EXISTS,
                    NotificationErrorCodes.NOTIF_TEMPLATE_CODE_DUPLICATE, request.getTemplateCode());
        }

        // fileFk stays NULL (DEFERRED, XM-NOTIF-001); isActiveFl defaults to true via the entity
        NotificationTemplate entity = templateMapper.toEntity(request);
        NotificationTemplate saved = templateRepository.save(entity);

        log.info("Created NotificationTemplate ID: {}", saved.getId());
        return ServiceResult.success(templateMapper.toResponse(saved), Status.CREATED);
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_TEMPLATE_UPDATE)")
    public ServiceResult<NotificationTemplateResponse> update(Long id, NotificationTemplateUpdateRequest request) {
        log.info("Updating NotificationTemplate ID: {}", id);

        NotificationTemplate entity = findOrThrow(id);
        assertBilingual(request.getTemplateBodyAr(), request.getTemplateBodyEn());

        // templateCode is untouched — RULE-NOTIF-007 (immutable), also excluded from the DTO body
        templateMapper.updateEntityFromRequest(entity, request);
        NotificationTemplate saved = templateRepository.save(entity);

        log.info("Updated NotificationTemplate ID: {}", saved.getId());
        return ServiceResult.success(templateMapper.toResponse(saved), Status.UPDATED);
    }

    /**
     * API-NOTIF-009. No usage/reference guard: NOTIF_LOG.TEMPLATE_CODE is a natural-key
     * soft-reference with NO physical FK (db-script.md governance note, RULE-NOTIF-006's own
     * fallback design) — deactivating an in-use template cannot violate referential integrity,
     * it just makes the fan-out processor's fallback path apply on the next send.
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).NOTIFICATION_TEMPLATE_DELETE)")
    public ServiceResult<NotificationTemplateResponse> deactivate(Long id) {
        log.info("Deactivating NotificationTemplate ID: {}", id);

        NotificationTemplate entity = findOrThrow(id);
        entity.deactivate();
        NotificationTemplate saved = templateRepository.save(entity);

        log.info("Deactivated NotificationTemplate ID: {}", saved.getId());
        return ServiceResult.success(templateMapper.toResponse(saved), Status.UPDATED);
    }

    private NotificationTemplate findOrThrow(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new LocalizedException(
                        Status.NOT_FOUND, NotificationErrorCodes.NOTIF_TEMPLATE_NOT_FOUND, id));
    }

    /** RULE-NOTIF-006 (create/update half) — ERR-NOTIF-0002, same precedent as FILE_NAME_REQUIRED. */
    private void assertBilingual(String bodyAr, String bodyEn) {
        if (bodyAr == null || bodyAr.isBlank() || bodyEn == null || bodyEn.isBlank()) {
            throw new LocalizedException(Status.BAD_REQUEST, NotificationErrorCodes.NOTIF_TEMPLATE_BILINGUAL_REQUIRED);
        }
    }
}
