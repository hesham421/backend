package com.example.erp.file.security;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.config.properties.FileTokenProperties;
import com.example.erp.file.exception.FileErrorCodes;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module-local security component (CORE.md "Encrypted Token layer" deviation, sourced from
 * ARCH-REF-1.10 AD-FILE-02/03): issues and validates the Encrypted Token embedded in the
 * {@code /upload/{token}}, {@code /download/{token}}, {@code /{token}} URL paths.
 *
 * AES/256-GCM, 12-byte random IV, 128-bit auth tag. NOT a JWT (POLICY-CLI-02/03/06) — this
 * token carries its own payload and TTL and is never validated by Security's filter chain.
 *
 * Payload shape (pipe-delimited, matches CORE.md "self-contained AES/GCM payload"):
 * {@code ownerId|ownerType|moduleCode|action|issuedAtEpochMilli|targetId}
 *
 * The single-use "consumed token" cache (RULE-FILE-004, POLICY-CLI-02) is an in-memory
 * {@link ConcurrentHashMap}, same acceptable-for-single-instance-deployment precedent as
 * {@code LoginRateLimiterService} in erp-security (deploy/docker-compose.yml runs exactly one
 * backend container) — if the backend is ever horizontally scaled, this must move to Redis.
 */
@Service
public class FileTokenService {

    public static final String ACTION_UPLOAD = "UPLOAD";
    public static final String ACTION_DOWNLOAD = "DOWNLOAD";
    public static final String ACTION_DELETE = "DELETE";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final String FIELD_DELIMITER = "\\|";
    private static final int PAYLOAD_FIELD_COUNT = 6;

    private final SecretKeySpec secretKey;
    private final long ttlMinutes;
    private final Map<String, Instant> consumedTokens = new ConcurrentHashMap<>();

    public FileTokenService(FileTokenProperties properties) {
        this.secretKey = deriveKey(properties.secret());
        this.ttlMinutes = properties.ttlMinutes();
    }

    /**
     * Issues a new single-use Encrypted Token (RULE-FILE-004 is satisfied by construction —
     * every call encodes a fresh, distinct payload with the current timestamp).
     */
    public FileTokenIssueResult issue(Long ownerId, String ownerType, String moduleCode,
                                       String action, Long targetId) {
        Instant now = Instant.now();
        String payload = String.join("|",
            String.valueOf(ownerId), ownerType, moduleCode, action,
            String.valueOf(now.toEpochMilli()), String.valueOf(targetId));

        String encryptedToken = encrypt(payload);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(now.plusSeconds(ttlMinutes * 60), ZoneOffset.UTC);
        return new FileTokenIssueResult(encryptedToken, expiresAt);
    }

    /**
     * Decodes, validates (RULE-FILE-002 TTL, RULE-FILE-003 tamper/action-mismatch, RULE-FILE-004
     * single-use), and marks the token consumed — all in one pass, since a token that fails any
     * check must never be treated as consumed (that would let a legitimate retry with the
     * correct token get spuriously rejected).
     */
    public FileTokenPayload decodeAndConsume(String encryptedToken, String expectedAction) {
        String plaintext = decrypt(encryptedToken);
        String[] parts = plaintext.split(FIELD_DELIMITER, -1);
        if (parts.length != PAYLOAD_FIELD_COUNT) {
            throw new LocalizedException(Status.UNAUTHORIZED, FileErrorCodes.FILE_TOKEN_INVALID);
        }

        Long ownerId;
        String ownerType = parts[1];
        String moduleCode = parts[2];
        String action = parts[3];
        long issuedAtEpochMilli;
        Long targetId;
        try {
            ownerId = Long.valueOf(parts[0]);
            issuedAtEpochMilli = Long.parseLong(parts[4]);
            targetId = Long.valueOf(parts[5]);
        } catch (NumberFormatException e) {
            throw new LocalizedException(Status.UNAUTHORIZED, FileErrorCodes.FILE_TOKEN_INVALID);
        }

        if (!expectedAction.equals(action)) {
            throw new LocalizedException(Status.FORBIDDEN, FileErrorCodes.FILE_TOKEN_ACTION_MISMATCH);
        }

        Instant issuedAt = Instant.ofEpochMilli(issuedAtEpochMilli);
        if (Instant.now().isAfter(issuedAt.plusSeconds(ttlMinutes * 60))) {
            throw new LocalizedException(Status.UNAUTHORIZED, FileErrorCodes.FILE_TOKEN_EXPIRED);
        }

        purgeExpiredConsumedEntries();
        Instant previouslyConsumedAt = consumedTokens.putIfAbsent(encryptedToken, issuedAt);
        if (previouslyConsumedAt != null) {
            throw new LocalizedException(Status.UNAUTHORIZED, FileErrorCodes.FILE_TOKEN_ALREADY_USED);
        }

        return new FileTokenPayload(ownerId, ownerType, moduleCode, targetId);
    }

    private void purgeExpiredConsumedEntries() {
        Instant cutoff = Instant.now().minusSeconds(ttlMinutes * 60);
        consumedTokens.values().removeIf(issuedAt -> issuedAt.isBefore(cutoff));
    }

    private static SecretKeySpec deriveKey(String secret) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to derive File Token secret key", e);
        }
    }

    private String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer combined = ByteBuffer.allocate(iv.length + cipherText.length);
            combined.put(iv).put(cipherText);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.array());
        } catch (GeneralSecurityException e) {
            throw new LocalizedException(Status.INTERNAL_ERROR, FileErrorCodes.FILE_TOKEN_ISSUE_FAILED);
        }
    }

    /**
     * RULE-FILE-003 — a missing, malformed, non-Base64, too-short, or GCM-tampered token
     * (including one a client tried to construct itself, per POLICY-CLI-03) fails here and is
     * rejected as 401, before any payload field is ever parsed.
     */
    private String decrypt(String encryptedToken) {
        try {
            byte[] combined = Base64.getUrlDecoder().decode(encryptedToken);
            if (combined.length <= GCM_IV_LENGTH_BYTES) {
                throw new LocalizedException(Status.UNAUTHORIZED, FileErrorCodes.FILE_TOKEN_INVALID);
            }
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] cipherText = new byte[combined.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(combined, GCM_IV_LENGTH_BYTES, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new LocalizedException(Status.UNAUTHORIZED, FileErrorCodes.FILE_TOKEN_INVALID);
        }
    }
}
