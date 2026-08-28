package com.erp.file.dto;

/**
 * Binary download result (API-FILE-003) — deliberately not a {@code @Schema} DTO like the rest
 * of this package: the controller streams {@code content} directly with headers, bypassing
 * {@code ApiResponseWrapper}, so the Controller never has to reference the entity directly.
 */
public record FileDownloadResult(byte[] content, String mimeType, String fileNameOriginal) {
}
