package com.erp.file.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.SecurityContextHelper;
import com.erp.file.dto.FileAccessTokenResponse;
import com.erp.file.entity.FileDocument;
import com.erp.file.exception.FileErrorCodes;
import com.erp.file.repository.FileDocumentRepository;
import com.erp.file.security.FileTokenIssueResult;
import com.erp.file.security.FileTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Issues the access token for {@code POST /api/v1/files/{fileDocumentPk}/access-token}, closing a plan gap. Unlike
 * upload/download this route requires a real JWT principal; RULE-FILE-007's owner-or-Admin check is enforced here at
 * issuance, not at the token-only delete endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileAccessTokenService {

    /**
     * String-literal reference to {@code SecurityPermissions.SYSTEM_ADMIN} (no compile dependency) —
     * needed as a plain boolean in a method body, not a SpEL annotation.
     */
    private static final String PERMISSION_SYSTEM_ADMIN = "PERM_SYSTEM_ADMIN";

    private static final Set<String> ALLOWED_ACTIONS =
        Set.of(FileTokenService.ACTION_DOWNLOAD, FileTokenService.ACTION_DELETE);

    private final FileDocumentRepository fileDocumentRepository;
    private final FileTokenService fileTokenService;

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<FileAccessTokenResponse> issueAccessToken(Long fileDocumentPk, String action) {
        log.info("Issuing {} access token for FileDocument ID: {}", action, fileDocumentPk);

        if (!ALLOWED_ACTIONS.contains(action)) {
            throw new LocalizedException(
                Status.VALIDATION_ERROR, FileErrorCodes.FILE_ACCESS_TOKEN_ACTION_INVALID, action);
        }

        FileDocument document = fileDocumentRepository.findById(fileDocumentPk)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND, FileErrorCodes.FILE_DOCUMENT_NOT_FOUND, fileDocumentPk));

        if (FileTokenService.ACTION_DELETE.equals(action)) {
            assertCanDelete();
        }

        FileTokenIssueResult tokenResult = fileTokenService.issue(
            document.getOwnerId(), document.getOwnerType(), document.getModuleCode(), action, document.getId());

        FileAccessTokenResponse response = FileAccessTokenResponse.builder()
            .encryptedToken(tokenResult.encryptedToken())
            .expiresAt(tokenResult.expiresAt())
            .build();

        return ServiceResult.success(response, Status.CREATED);
    }

    /**
     * RULE-FILE-007 — Admin half only. The owning-actor half can't be resolved: uploads always run
     * on the permitAll'd, token-only route with no JWT principal, so {@code createdBy} is always
     * "system" — that check would need a cross-module callback this plan doesn't define.
     */
    private void assertCanDelete() {
        if (!SecurityContextHelper.hasAuthority(PERMISSION_SYSTEM_ADMIN)) {
            // TODO: OQ-FILE-001 — "owning entity's authorized actor" half of RULE-FILE-007 has
            // no resolvable signal today; Admin-only enforced until a cross-module authorization
            // check is designed (see execution-state.json blocked[]).
            throw new LocalizedException(Status.FORBIDDEN, FileErrorCodes.FILE_DELETE_NOT_AUTHORIZED);
        }
    }
}
