package com.example.erp.file.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.dto.FileUploadTokenRequest;
import com.example.erp.file.dto.FileUploadTokenResponse;
import com.example.erp.file.entity.FileCategory;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileCategoryRepository;
import com.example.erp.file.security.FileTokenIssueResult;
import com.example.erp.file.security.FileTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates API-FILE-001 (Issue Upload Token). No persistence — the token is stateless
 * (CORE.md), so this is the one exception to the standard create()/find()/save() Service
 * template: it validates {@code FileCategory} existence (QR-FILE-001), then delegates
 * encoding to the module-local {@link FileTokenService} security component.
 *
 * {@code @PreAuthorize} uses a string literal, not {@code T(SecurityPermissions)} — erp-file has
 * no compile dependency on erp-security (module-registry-filesvc.md A7: "no SHARED entity
 * consumed from another module"), same no-compile-dependency convention as erp-finance-gl's
 * {@code AccountsChartService} and this module's own {@link FileAccessTokenService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadTokenService {

    private static final String ACTION_UPLOAD = "UPLOAD";

    private final FileCategoryRepository fileCategoryRepository;
    private final FileTokenService fileTokenService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PERM_FILE_ATTACHMENT_CREATE')")
    public ServiceResult<FileUploadTokenResponse> issueUploadToken(FileUploadTokenRequest request) {
        log.info("Issuing upload token for ownerType={}, moduleCode={}, fileCategoryFk={}",
            request.getOwnerType(), request.getModuleCode(), request.getFileCategoryFk());

        FileCategory fileCategory = fileCategoryRepository.findById(request.getFileCategoryFk())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FileErrorCodes.FILE_CATEGORY_NOT_FOUND, request.getFileCategoryFk()));

        FileTokenIssueResult tokenResult = fileTokenService.issue(
            request.getOwnerId(), request.getOwnerType(), request.getModuleCode(),
            ACTION_UPLOAD, fileCategory.getId());

        FileUploadTokenResponse response = FileUploadTokenResponse.builder()
            .encryptedToken(tokenResult.encryptedToken())
            .expiresAt(tokenResult.expiresAt())
            .build();

        return ServiceResult.success(response, Status.CREATED);
    }
}
