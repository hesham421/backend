package com.erp.file.security;

/**
 * Decoded Encrypted Token payload (API-FILE-002/003/004). {@code targetId}'s meaning depends on
 * the issuing action: the upload token carries {@code fileCategoryFk} (the file doesn't exist
 * yet); the download/delete token carries {@code fileDocumentPk}.
 */
public record FileTokenPayload(Long ownerId, String ownerType, String moduleCode, Long targetId) {
}
