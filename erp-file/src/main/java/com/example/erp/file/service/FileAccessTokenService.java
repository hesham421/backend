package com.example.erp.file.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.common.util.SecurityContextHelper;
import com.example.erp.file.dto.FileAccessTokenResponse;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileDocumentRepository;
import com.example.erp.file.security.FileTokenIssueResult;
import com.example.erp.file.security.FileTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Orchestrates the new {@code POST /api/v1/files/{fileDocumentPk}/access-token} endpoint —
 * added to close a plan gap: API-FILE-003/004 both require a token whose payload carries
 * {@code fileDocumentPk}, but the plan's only defined token-issuing endpoint (API-FILE-001)
 * is scoped to pre-upload {@code fileCategoryFk} tokens and cannot address an existing file.
 * See execution-state.json notes for the full writeup and user sign-off.
 *
 * Unlike {@link FileUploadService}/{@link FileDownloadService}, this route is a standard
 * {@code /api/v1/files/...} endpoint — NOT permitAll'd — so a real JWT principal exists and
 * {@code @PreAuthorize("isAuthenticated()")} is meaningful here, same posture as
 * {@link FileUploadTokenService}.
 *
 * RULE-FILE-007 (owner-or-Admin delete restriction) is enforced HERE, at issuance time, not at
 * the actual {@code DELETE /{token}} consumption endpoint — that route is permitAll'd/token-only
 * (POLICY-CLI-06), so it has no JWT principal to check against. Possessing a valid DELETE
 * token IS the authorization from that point on, same pattern as the upload token already being
 * "pre-authorized intent". User-approved resolution (2026-07-13), see execution-state.json.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileAccessTokenService {

    /**
     * String-literal reference to {@code com.example.security.constants.SecurityPermissions
     * .SYSTEM_ADMIN} — same no-compile-dependency convention this codebase already uses for
     * cross-module permission references via {@code @PreAuthorize} SpEL (e.g. erp-finance-gl's
     * {@code AccountsChartService}); this check needs a plain boolean in a method body instead
     * of an annotation, so the literal is held here rather than in a SpEL string.
     */
    private static final String PERMISSION_SYSTEM_ADMIN = "PERM_SYSTEM_ADMIN";

    // Same string-literal convention as PERMISSION_SYSTEM_ADMIN above (no erp-security compile
    // dependency) — action-conditional, so held as plain constants for a method-body check
    // (SEC.md: API-FILE-003/005-equivalent DOWNLOAD needs VIEW, API-FILE-004-equivalent DELETE
    // needs DELETE) rather than a single static @PreAuthorize SpEL expression.
    private static final String PERMISSION_FILE_ATTACHMENT_VIEW = "PERM_FILE_ATTACHMENT_VIEW";
    private static final String PERMISSION_FILE_ATTACHMENT_DELETE = "PERM_FILE_ATTACHMENT_DELETE";

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

        assertHasPermissionForAction(action);

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
     * SEC.md API-level enforcement — DOWNLOAD requires PERM_FILE_ATTACHMENT_VIEW, DELETE
     * requires PERM_FILE_ATTACHMENT_DELETE. Checked here (issuance time, real JWT principal),
     * not at the permitAll'd /download or /{token} DELETE consumption routes — same rationale
     * as RULE-FILE-007's assertCanDelete() below. Thrown as AccessDeniedException, same as a
     * failing @PreAuthorize would produce natively (GlobalExceptionHandler already maps it to
     * 403 FORBIDDEN platform-wide) — not a new module-specific error code.
     */
    private void assertHasPermissionForAction(String action) {
        String requiredPermission = FileTokenService.ACTION_DOWNLOAD.equals(action)
            ? PERMISSION_FILE_ATTACHMENT_VIEW
            : PERMISSION_FILE_ATTACHMENT_DELETE;
        if (!SecurityContextHelper.hasAuthority(requiredPermission)) {
            throw new AccessDeniedException("Missing required permission: " + requiredPermission);
        }
    }

    /**
     * RULE-FILE-007 — Admin half only. The "owning entity's authorized actor" half cannot be
     * resolved: {@code FileDocument.ownerId} addresses a business record in another module (no
     * FK, no data dependency by design — module-registry-filesvc.md), and the only user-identity
     * trail on the entity ({@code createdBy}) is always "system" because upload itself happens
     * on the permitAll'd, token-only {@code /upload/{token}} route with no JWT principal to
     * record. Answering "is this user authorized on the owning record" would require a
     * cross-module callback this plan never defines.
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
