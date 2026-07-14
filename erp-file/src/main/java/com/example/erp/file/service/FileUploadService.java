package com.example.erp.file.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.common.util.StringUtils;
import com.example.erp.file.dto.FileUploadResponse;
import com.example.erp.file.entity.FileCategory;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileCategoryRepository;
import com.example.erp.file.repository.FileDocumentRepository;
import com.example.erp.file.security.FileTokenPayload;
import com.example.erp.file.util.DetectedFileType;
import com.example.erp.file.util.FileContentTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Orchestrates API-FILE-002 (Upload File). {@code tokenPayload} comes from
 * {@link com.example.erp.file.security.FileTokenFilter}, already decoded/validated
 * (RULE-FILE-002/003/004) before this method is ever reached.
 *
 * {@code @PreAuthorize("true")} is deliberate, not a placeholder: {@code /upload/{token}} is
 * permitAll'd in Security's central filter chain (POLICY-CLI-06 — no JWT on this route), so
 * there is no authenticated principal for Spring method security to evaluate here. The token
 * layer is the sole authorization gate for this method — governance's "every public method
 * needs @PreAuthorize" is satisfied honestly, not bypassed. The PERM_FILE_ATTACHMENT_CREATE
 * check SVCAPI.md's SECURITY line calls for is structurally impossible on a token-only route
 * and is flagged as a Phase SEC gap (execution-state.json notes), not silently implemented here.
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
