package com.example.erp.file.dto;

/**
 * Binary download result (API-FILE-003) — deliberately not a {@code @Schema}-annotated DTO
 * like the rest of this package: it never serializes to JSON (the controller streams
 * {@code content} directly with {@code mimeType}/{@code fileNameOriginal} in HTTP headers,
 * bypassing {@code ApiResponseWrapper} per its own "don't wrap binary responses" rule). Exists
 * only so the Controller never has to reference the {@code FileDocument} entity directly
 * (create-controller: "MUST NOT reference entity classes").
 */
public record FileDownloadResult(byte[] content, String mimeType, String fileNameOriginal) {
}
