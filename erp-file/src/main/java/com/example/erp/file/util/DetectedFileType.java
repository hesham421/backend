package com.example.erp.file.util;

/**
 * Result of {@link FileContentTypeDetector#detect} — {@code fileTypeId} is one of
 * LOV-FILE-001's codes (IMAGE/DOCUMENT/SPREADSHEET/ARCHIVE/OTHER, srs.md A5).
 */
public record DetectedFileType(String mimeType, String fileTypeId) {
}
