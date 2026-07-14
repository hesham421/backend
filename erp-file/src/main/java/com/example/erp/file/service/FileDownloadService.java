package com.example.erp.file.service;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.dto.FileDownloadResult;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileDocumentRepository;
import com.example.erp.file.security.FileTokenPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates API-FILE-003 (Download File). See {@link FileUploadService}'s class-level
 * javadoc for why {@code @PreAuthorize("true")} is the correct, deliberate annotation here too
 * — {@code /download/{token}} is permitAll'd; the token layer is the sole gate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileDownloadService {

    private final FileDocumentRepository fileDocumentRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("true")
    public FileDownloadResult getForDownload(FileTokenPayload tokenPayload) {
        log.debug("Downloading FileDocument ID: {}", tokenPayload.targetId());

        FileDocument document = fileDocumentRepository.findById(tokenPayload.targetId())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FileErrorCodes.FILE_DOCUMENT_NOT_FOUND, tokenPayload.targetId()));

        // RULE-FILE-006 / DRV-FILE-003 — a purged file returns 410 GONE, not a null-content stream.
        if (FileDocument.STATUS_DELETED.equals(document.getFileStatusId())) {
            throw new LocalizedException(
                Status.GONE, FileErrorCodes.FILE_NO_LONGER_AVAILABLE, tokenPayload.targetId());
        }

        return new FileDownloadResult(document.getFileContent(), document.getMimeType(),
            document.getFileNameOriginal());
    }
}
