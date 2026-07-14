package com.example.erp.file.service;

import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.config.properties.FileTokenProperties;
import com.example.erp.file.dto.FileAccessTokenResponse;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileDocumentRepository;
import com.example.erp.file.security.FileTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileAccessTokenServiceTest {

    @Mock
    private FileDocumentRepository fileDocumentRepository;

    private FileAccessTokenService service;

    @BeforeEach
    void setUp() {
        FileTokenService fileTokenService =
            new FileTokenService(new FileTokenProperties("test-file-token-secret-value", 100));
        service = new FileAccessTokenService(fileDocumentRepository, fileTokenService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void issueAccessToken_validDownloadAction_returnsToken() {
        FileDocument document = FileDocument.builder()
            .id(501L).ownerId(1001L).ownerType("PURCHASE_ORDER").moduleCode("PRC").build();
        when(fileDocumentRepository.findById(501L)).thenReturn(Optional.of(document));

        FileAccessTokenResponse response = service.issueAccessToken(501L, "DOWNLOAD").getData();

        assertThat(response.getEncryptedToken()).isNotBlank();
        assertThat(response.getExpiresAt()).isNotNull();
    }

    @Test
    void issueAccessToken_invalidAction_throwsValidationError() {
        assertThatThrownBy(() -> service.issueAccessToken(501L, "UPLOAD"))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_ACCESS_TOKEN_ACTION_INVALID));
    }

    @Test
    void issueAccessToken_unknownFileDocument_throwsNotFound() {
        when(fileDocumentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issueAccessToken(99L, "DELETE"))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_DOCUMENT_NOT_FOUND));
    }

    @Test
    void issueAccessToken_deleteActionAsAdmin_returnsToken() {
        FileDocument document = FileDocument.builder()
            .id(501L).ownerId(1001L).ownerType("PURCHASE_ORDER").moduleCode("PRC").build();
        when(fileDocumentRepository.findById(501L)).thenReturn(Optional.of(document));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(
            "admin", null, List.of(new SimpleGrantedAuthority("PERM_SYSTEM_ADMIN"))));

        FileAccessTokenResponse response = service.issueAccessToken(501L, "DELETE").getData();

        assertThat(response.getEncryptedToken()).isNotBlank();
    }

    @Test
    void issueAccessToken_deleteActionWithoutAdminAuthority_throwsForbidden() {
        FileDocument document = FileDocument.builder()
            .id(501L).ownerId(1001L).ownerType("PURCHASE_ORDER").moduleCode("PRC").build();
        when(fileDocumentRepository.findById(501L)).thenReturn(Optional.of(document));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(
            "regular_user", null, List.of(new SimpleGrantedAuthority("PERM_FILE_ATTACHMENT_VIEW"))));

        assertThatThrownBy(() -> service.issueAccessToken(501L, "DELETE"))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_DELETE_NOT_AUTHORIZED));
    }
}
