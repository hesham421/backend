package com.example.erp.file.security;

import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.config.properties.FileTokenProperties;
import com.example.erp.file.exception.FileErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileTokenServiceTest {

    private FileTokenService service;

    @BeforeEach
    void setUp() {
        service = new FileTokenService(new FileTokenProperties("test-file-token-secret-value", 100));
    }

    @Test
    void issue_returnsNonBlankTokenAndExpiryAroundTtl() {
        FileTokenIssueResult result = service.issue(1001L, "PURCHASE_ORDER", "PRC", "UPLOAD", 3L);

        assertThat(result.encryptedToken()).isNotBlank();
        LocalDateTime expected = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(100);
        assertThat(result.expiresAt()).isBetween(expected.minusSeconds(5), expected.plusSeconds(5));
    }

    @Test
    void issue_producesDistinctTokensForConsecutiveCalls() {
        FileTokenIssueResult first = service.issue(1001L, "PURCHASE_ORDER", "PRC", "UPLOAD", 3L);
        FileTokenIssueResult second = service.issue(1001L, "PURCHASE_ORDER", "PRC", "UPLOAD", 3L);

        assertThat(first.encryptedToken()).isNotEqualTo(second.encryptedToken());
    }

    @Test
    void decodeAndConsume_validToken_returnsPayloadMatchingIssuedValues() {
        FileTokenIssueResult issued = service.issue(1001L, "PURCHASE_ORDER", "PRC", "UPLOAD", 3L);

        FileTokenPayload payload = service.decodeAndConsume(issued.encryptedToken(), "UPLOAD");

        assertThat(payload.ownerId()).isEqualTo(1001L);
        assertThat(payload.ownerType()).isEqualTo("PURCHASE_ORDER");
        assertThat(payload.moduleCode()).isEqualTo("PRC");
        assertThat(payload.targetId()).isEqualTo(3L);
    }

    @Test
    void decodeAndConsume_actionMismatch_throwsForbidden() {
        FileTokenIssueResult issued = service.issue(1001L, "PURCHASE_ORDER", "PRC", "DOWNLOAD", 3L);

        assertThatThrownBy(() -> service.decodeAndConsume(issued.encryptedToken(), "DELETE"))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_TOKEN_ACTION_MISMATCH));
    }

    @Test
    void decodeAndConsume_tamperedToken_throwsInvalid() {
        FileTokenIssueResult issued = service.issue(1001L, "PURCHASE_ORDER", "PRC", "UPLOAD", 3L);
        String tampered = issued.encryptedToken().substring(0, issued.encryptedToken().length() - 2) + "xx";

        assertThatThrownBy(() -> service.decodeAndConsume(tampered, "UPLOAD"))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_TOKEN_INVALID));
    }

    @Test
    void decodeAndConsume_reusedToken_throwsAlreadyUsed() {
        FileTokenIssueResult issued = service.issue(1001L, "PURCHASE_ORDER", "PRC", "UPLOAD", 3L);
        service.decodeAndConsume(issued.encryptedToken(), "UPLOAD");

        assertThatThrownBy(() -> service.decodeAndConsume(issued.encryptedToken(), "UPLOAD"))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_TOKEN_ALREADY_USED));
    }
}
