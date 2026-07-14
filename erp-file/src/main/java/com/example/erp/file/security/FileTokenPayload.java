package com.example.erp.file.security;

/**
 * Decoded, validated Encrypted Token payload (API-FILE-002/003/004) — set as a request
 * attribute by {@link FileTokenFilter} for the Controller/Service to consume as a plain
 * argument. {@code action} is intentionally omitted — it was already verified against the
 * calling endpoint by {@link FileTokenService#decodeAndConsume}.
 *
 * {@code targetId} meaning depends on the action the token was issued for: the upload token
 * (API-FILE-001) carries {@code fileCategoryFk} (the file doesn't exist yet); the download/
 * delete access token (new {@code POST /api/v1/files/{fileDocumentPk}/access-token} endpoint,
 * added to close the plan gap where API-FILE-003/004 require a fileDocumentPk-addressed token
 * but no issuing endpoint for one was ever defined) carries {@code fileDocumentPk}.
 */
public record FileTokenPayload(Long ownerId, String ownerType, String moduleCode, Long targetId) {
}
