package com.example.erp.file.service;

import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.Op;
import com.erp.common.search.PageableBuilder;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.util.StringUtils;
import com.example.erp.file.dto.FileDocumentSummaryResponse;
import com.example.erp.file.entity.FileCategory;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.repository.FileDocumentRepository;
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
 * Orchestrates API-FILE-005 (List Files for Owner Record) — {@code GET /api/v1/files/{ownerId}}.
 * A standard, non-token, JWT-authenticated route (unlike upload/download/delete), so
 * {@code @PreAuthorize("isAuthenticated()")} reflects a real principal here — same posture as
 * API-FILE-001, deferring the fine-grained PERM_FILE_ATTACHMENT_VIEW check to Phase SEC (not yet
 * run for this module; the permission constant doesn't exist in SecurityPermissions yet).
 *
 * The endpoint is a plain GET with path/query params (create-controller A.6.6: no GET +
 * @ModelAttribute) — {@link SearchRequest} (the plain internal DTO SpecBuilder/PageableBuilder
 * consume) is built manually here from those params rather than bound automatically, so this
 * still satisfies A.5.7's "search uses SpecBuilder + PageableBuilder" without a body-oriented
 * BaseSearchContractRequest DTO the transport shape doesn't actually match.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileListService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "fileNameOriginal", "fileSizeBytes");

    // ownerId/ownerType are the fixed, server-controlled filters this endpoint applies
    // (QR-FILE-007) — not user-supplied sort/filter fields, so they get their own whitelist,
    // separate from ALLOWED_SORT_FIELDS which governs sorting only.
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of("ownerId", "ownerType");

    private final FileDocumentRepository fileDocumentRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<Page<FileDocumentSummaryResponse>> listByOwner(
            Long ownerId, String ownerType, int page, int size, String sortBy, String sortDir) {
        log.debug("Listing files for ownerId={}, ownerType={}", ownerId, ownerType);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDir(sortDir);
        searchRequest.addFilter(new SearchFilter("ownerId", Op.EQ, ownerId));
        if (StringUtils.isNotBlank(ownerType)) {
            searchRequest.addFilter(new SearchFilter("ownerType", Op.EQ, ownerType));
        }

        Specification<FileDocument> spec = SpecBuilder.build(
            searchRequest, new SetAllowedFields(ALLOWED_FILTER_FIELDS), DefaultFieldValueConverter.INSTANCE);
        Pageable pageable = PageableBuilder.from(searchRequest, ALLOWED_SORT_FIELDS);

        Page<FileDocument> results = fileDocumentRepository.findAll(spec, pageable);
        return ServiceResult.success(results.map(this::toSummary));
    }

    private FileDocumentSummaryResponse toSummary(FileDocument document) {
        FileCategory category = document.getFileCategory();
        return FileDocumentSummaryResponse.builder()
            .fileDocumentPk(document.getId())
            .fileNameOriginal(document.getFileNameOriginal())
            .fileCategoryFk(category != null ? category.getId() : null)
            .fileCategoryNameAr(category != null ? category.getNameAr() : null)
            .fileCategoryNameEn(category != null ? category.getNameEn() : null)
            .fileTypeId(document.getFileTypeId())
            .fileSizeBytes(document.getFileSizeBytes())
            .fileStatusId(document.getFileStatusId())
            .createdAt(document.getCreatedAt())
            .build();
    }
}
