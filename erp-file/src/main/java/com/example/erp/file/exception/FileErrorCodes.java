package com.example.erp.file.exception;

/**
 * Centralized Error Codes for File Service Module.
 *
 * All codes must have corresponding messages in:
 * - erp-main/src/main/resources/i18n/messages.properties (English)
 * - erp-main/src/main/resources/i18n/messages_ar.properties (Arabic)
 */
public final class FileErrorCodes {

    private FileErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== File Category Errors ====================
    // ERR-FILE-0009 (API-FILE-001)
    public static final String FILE_CATEGORY_NOT_FOUND = "FILE_CATEGORY_NOT_FOUND";

    // ==================== File Document Errors ====================
    public static final String FILE_DOCUMENT_NOT_FOUND = "FILE_DOCUMENT_NOT_FOUND";
    // ERR-FILE-0001 (RULE-FILE-001, API-FILE-002)
    public static final String FILE_SIZE_EXCEEDED = "FILE_SIZE_EXCEEDED";
    // ERR-FILE-0008 (RULE-FILE-006/DRV-FILE-003, API-FILE-003 — download of a purged file)
    public static final String FILE_NO_LONGER_AVAILABLE = "FILE_NO_LONGER_AVAILABLE";
    public static final String FILE_UPLOAD_READ_FAILED = "FILE_UPLOAD_READ_FAILED";
    public static final String FILE_NAME_REQUIRED = "FILE_NAME_REQUIRED";

    // ==================== Encrypted Token Errors ====================
    public static final String FILE_TOKEN_ISSUE_FAILED = "FILE_TOKEN_ISSUE_FAILED";
    // ERR-FILE-0002 (RULE-FILE-002)
    public static final String FILE_TOKEN_EXPIRED = "FILE_TOKEN_EXPIRED";
    // ERR-FILE-0003 (RULE-FILE-003 — missing/tampered/unparseable, 401)
    public static final String FILE_TOKEN_INVALID = "FILE_TOKEN_INVALID";
    // ERR-FILE-0003 (RULE-FILE-003 — action mismatch, 403; same rule, distinct HTTP outcome
    // per SVCAPI.md's ERRORS note, so kept as a separate semantic code from FILE_TOKEN_INVALID)
    public static final String FILE_TOKEN_ACTION_MISMATCH = "FILE_TOKEN_ACTION_MISMATCH";
    // RULE-FILE-004 (no numbered ERR-ID in SVCAPI.md's API-FILE-001 ERRORS section)
    public static final String FILE_TOKEN_ALREADY_USED = "FILE_TOKEN_ALREADY_USED";

    // ==================== File Access Token Errors ====================
    // New POST /api/v1/files/{fileDocumentPk}/access-token endpoint (added to close the
    // download/delete token-issuance gap — see execution-state.json notes)
    public static final String FILE_ACCESS_TOKEN_ACTION_INVALID = "FILE_ACCESS_TOKEN_ACTION_INVALID";
    // ERR-FILE-0007 (RULE-FILE-007 — Admin half only; see OQ-FILE-001 in execution-state.json)
    public static final String FILE_DELETE_NOT_AUTHORIZED = "FILE_DELETE_NOT_AUTHORIZED";
}
