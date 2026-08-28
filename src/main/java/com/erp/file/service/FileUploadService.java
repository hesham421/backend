package com.erp.file.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.StringUtils;
import com.erp.file.dto.FileUploadResponse;
import com.erp.file.entity.FileCategory;
import com.erp.file.entity.FileDocument;
import com.erp.file.exception.FileErrorCodes;
import com.erp.file.repository.FileCategoryRepository;
import com.erp.file.repository.FileDocumentRepository;
import com.erp.file.security.FileTokenPayload;
import com.erp.file.util.DetectedFileType;
import com.erp.file.util.FileContentTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Orchestrates API-FILE-002; {@code tokenPayload} is already decoded/validated by {@link
 * com.erp.file.security.FileTokenFilter}. {@code @PreAuthorize("true")} is deliberate: {@code /upload/{token}} is
 * permitAll'd, so the token layer is the sole auth gate (PERM_FILE_ATTACHMENT_CREATE is a tracked gap).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final FileCategoryRepository fileCategoryRepository;
    private final FileDocumentRepository fileDocumentRepository;

    @Transactional
    @PreAuthorize("true")
    public ServiceResult<FileUploadResponse> upload(FileTokenPayload tokenPayload, MultipartFile file) {
        log.info("Uploading file for ownerType={}, moduleCode={}, fileCategoryFk={}",
            tokenPayload.ownerType(), tokenPayload.moduleCode(), tokenPayload.targetId());

        FileCategory fileCategory = fileCategoryRepository.findById(tokenPayload.targetId())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FileErrorCodes.FILE_CATEGORY_NOT_FOUND, tokenPayload.targetId()));

        String fileNameOriginal = file.getOriginalFilename();
        if (StringUtils.isBlank(fileNameOriginal)) {
            throw new LocalizedException(Status.BAD_REQUEST, FileErrorCodes.FILE_NAME_REQUIRED);
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new LocalizedException(Status.BAD_REQUEST, FileErrorCodes.FILE_UPLOAD_READ_FAILED);
        }

        long maxSizeBytes = fileCategory.resolveMaxSizeBytes();
        if (content.length > maxSizeBytes) {
            throw new LocalizedException(Status.BAD_REQUEST, FileErrorCodes.FILE_SIZE_EXCEEDED, maxSizeBytes);
        }

        DetectedFileType detected = FileContentTypeDetector.detect(content);

        FileDocument document = FileDocument.builder()
            .ownerId(tokenPayload.ownerId())
            .ownerType(tokenPayload.ownerType())
            .moduleCode(tokenPayload.moduleCode())
            .fileCategory(fileCategory)
            .fileTypeId(detected.fileTypeId())
            .fileNameOriginal(fileNameOriginal)
            .mimeType(detected.mimeType())
            .fileSizeBytes((long) content.length)
            .fileContent(content)
            .fileStatusId(FileDocument.STATUS_ACTIVE)
            .build();

        FileDocument saved = fileDocumentRepository.save(document);
        log.info("Uploaded FileDocument ID: {}", saved.getId());

        FileUploadResponse response = FileUploadResponse.builder()
            .fileDocumentPk(saved.getId())
            .fileNameOriginal(saved.getFileNameOriginal())
            .fileTypeId(saved.getFileTypeId())
            .fileSizeBytes(saved.getFileSizeBytes())
            .fileStatusId(saved.getFileStatusId())
            .build();

        return ServiceResult.success(response, Status.CREATED);
    }
}
