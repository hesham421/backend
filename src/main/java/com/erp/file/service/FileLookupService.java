package com.erp.file.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.file.domain.FileDocumentDomain;
import com.erp.file.dto.LookupOptionResponse;
import com.erp.file.exception.FileErrorCodes;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API-FILE-008 — runtime resolution of the FILE-local LOVs (LOV-FILE-001 FILE_FILE_TYPE,
 * LOV-FILE-002 FILE_FILE_STATUS). These are code lists resolved at runtime (no lookup table per SRS
 * A5), so the options and their bilingual labels (SRS A5) are held in-process. An unknown lookupKey
 * yields ERR-0006 NOT_FOUND.
 *
 * <p>{@code @PreAuthorize("isAuthenticated()")} — spec-mandated form (API-FILE-008 SECURITY =
 * "Security filter"): any authenticated caller may read these platform lookups; they are not gated
 * by a FILE permission.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileLookupService {

    public static final String LOOKUP_FILE_TYPE = "FILE_FILE_TYPE";
    public static final String LOOKUP_FILE_STATUS = "FILE_FILE_STATUS";

    // LOV-FILE-001 type-bucket codes — single source of truth, also consumed by
    // FileService.deriveFileType() so the classifier and this LOV can never drift.
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_DOCUMENT = "DOCUMENT";
    public static final String TYPE_SPREADSHEET = "SPREADSHEET";
    public static final String TYPE_ARCHIVE = "ARCHIVE";
    public static final String TYPE_OTHER = "OTHER";

    // FILE_STATUS codes are NOT redefined here: they are sourced from FileDocumentDomain
    // (LOV-FILE-002 lifecycle owner) so the state machine and this LOV stay in lockstep.
    private static final Map<String, List<LookupOptionResponse>> LOOKUPS = Map.of(
        LOOKUP_FILE_TYPE, List.of(
            option(TYPE_IMAGE, "صورة", "Image"),
            option(TYPE_DOCUMENT, "مستند", "Document"),
            option(TYPE_SPREADSHEET, "جدول", "Spreadsheet"),
            option(TYPE_ARCHIVE, "أرشيف", "Archive"),
            option(TYPE_OTHER, "أخرى", "Other")),
        LOOKUP_FILE_STATUS, List.of(
            option(FileDocumentDomain.STATUS_ACTIVE, "نشط", "Active"),
            option(FileDocumentDomain.STATUS_ARCHIVED, "مؤرشف", "Archived"),
            option(FileDocumentDomain.STATUS_DELETED, "محذوف", "Deleted")));

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ServiceResult<List<LookupOptionResponse>> get(String lookupKey) {
        log.debug("Resolving FILE lookup for key: {}", lookupKey);

        String normalized = lookupKey == null ? null : lookupKey.trim().toUpperCase();
        List<LookupOptionResponse> options = normalized == null ? null : LOOKUPS.get(normalized);
        if (options == null) {
            throw new LocalizedException(
                Status.NOT_FOUND, FileErrorCodes.FILE_LOOKUP_KEY_UNKNOWN, lookupKey);
        }

        return ServiceResult.success(options);
    }

    private static LookupOptionResponse option(String code, String labelAr, String labelEn) {
        return LookupOptionResponse.builder().code(code).labelAr(labelAr).labelEn(labelEn).build();
    }
}
