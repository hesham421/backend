package com.erp.file.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.file.dto.FileUploadTokenRequest;
import com.erp.file.dto.FileUploadTokenResponse;
import com.erp.file.entity.FileCategory;
import com.erp.file.exception.FileErrorCodes;
import com.erp.file.repository.FileCategoryRepository;
import com.erp.file.security.FileTokenIssueResult;
import com.erp.file.security.FileTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates API-FILE-001. No persistence — the token is stateless, so this is the one
 * exception to the standard create()/find()/save() Service template.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadTokenService {

    private static final String ACTION_UPLOAD = "UPLOAD";

    private final FileCategoryRepository fileCategoryRepository;
    private final FileTokenService fileTokenService;

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
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
