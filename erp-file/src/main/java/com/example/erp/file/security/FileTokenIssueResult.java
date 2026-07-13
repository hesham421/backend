package com.example.erp.file.security;

import java.time.LocalDateTime;

/**
 * Result of issuing an Encrypted Token (API-FILE-001) — the encrypted, opaque token string
 * plus its computed expiry, for the Service layer to map onto {@code FileUploadTokenResponse}.
 */
public record FileTokenIssueResult(String encryptedToken, LocalDateTime expiresAt) {
}
