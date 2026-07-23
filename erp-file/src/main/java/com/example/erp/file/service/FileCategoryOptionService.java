package com.example.erp.file.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.file.dto.FileCategoryOptionResponse;
import com.example.erp.file.entity.FileCategory;
import com.example.erp.file.repository.FileCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestrates {@code GET /api/v1/files/categories} — the FileCategory dropdown-options
 * endpoint SCR-FILE-001's upload form needs (F2.md's FileCategoryService.listOptionsByModule),
 * closing GAP-FILE-003. Not a governed LOV (F2.md: "reuses LOV-style loader against FileCategory
 * table, not MD_LOOKUP_DETAIL" — FileCategory is a Reference Table per DATA+DOM, not a lookup).
 *
 * Standard, non-token, JWT-authenticated route (same posture as {@link FileListService}) — gated
 * on PERM_FILE_ATTACHMENT_VIEW: this only feeds a read-only dropdown consumed by a panel a user
 * must already hold that permission to see at all (SEC.md screen guard).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileCategoryOptionService {

    private final FileCategoryRepository fileCategoryRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_FILE_ATTACHMENT_VIEW')")
    public ServiceResult<List<FileCategoryOptionResponse>> listOptionsByModule(String moduleCode) {
        log.debug("Listing active file categories for moduleCode={}", moduleCode);

        List<FileCategory> categories = fileCategoryRepository.findByModuleCodeAndIsActiveFlTrueOrderByNameEnAsc(moduleCode);

        List<FileCategoryOptionResponse> options = categories.stream()
            .map(this::toOption)
            .toList();

        return ServiceResult.success(options);
    }

    private FileCategoryOptionResponse toOption(FileCategory category) {
        return FileCategoryOptionResponse.builder()
            .fileCategoryPk(category.getId())
            .categoryCode(category.getCategoryCode())
            .nameAr(category.getNameAr())
            .nameEn(category.getNameEn())
            .moduleCode(category.getModuleCode())
            .build();
    }
}
