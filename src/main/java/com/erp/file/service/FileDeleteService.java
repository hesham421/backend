package com.erp.file.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.file.dto.FileDeleteConfirmation;
import com.erp.file.entity.FileDocument;
import com.erp.file.exception.FileErrorCodes;
import com.erp.file.repository.FileDocumentRepository;
import com.erp.file.security.FileTokenPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates API-FILE-004 (Delete File). {@code /{token}} is permitAll'd — the token layer is
 * the sole gate; RULE-FILE-007 (owner-or-Admin) was already enforced when the token was issued
 * ({@link FileAccessTokenService}), not re-checked at consumption time.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileDeleteService {

    private final FileDocumentRepository fileDocumentRepository;

    @Transactional
    @PreAuthorize("true")
    public ServiceResult<FileDeleteConfirmation> delete(FileTokenPayload tokenPayload) {
        log.info("Deleting FileDocument ID: {}", tokenPayload.targetId());

        FileDocument document = fileDocumentRepository.findById(tokenPayload.targetId())
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FileErrorCodes.FILE_DOCUMENT_NOT_FOUND, tokenPayload.targetId()));

        document.purgeContent();
        FileDocument saved = fileDocumentRepository.save(document);
        log.info("Deleted (content purged) FileDocument ID: {}", saved.getId());

        FileDeleteConfirmation confirmation = FileDeleteConfirmation.builder()
            .fileDocumentPk(saved.getId())
            .fileStatusId(saved.getFileStatusId())
            .build();

        return ServiceResult.success(confirmation, Status.UPDATED);
    }
}
