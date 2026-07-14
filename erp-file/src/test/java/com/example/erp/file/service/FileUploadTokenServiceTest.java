package com.example.erp.file.service;

import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.config.properties.FileTokenProperties;
import com.example.erp.file.dto.FileUploadTokenRequest;
import com.example.erp.file.dto.FileUploadTokenResponse;
import com.example.erp.file.entity.FileCategory;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileCategoryRepository;
import com.example.erp.file.security.FileTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadTokenServiceTest {

    @Mock
    private FileCategoryRepository fileCategoryRepository;

    private FileUploadTokenService service;

    @BeforeEach
    void setUp() {
        FileTokenService fileTokenService =
            new FileTokenService(new FileTokenProperties("test-file-token-secret-value", 100));
        service = new FileUploadTokenService(fileCategoryRepository, fileTokenService);
    }

    @Test
    void issueUploadToken_existingFileCategory_returnsEncryptedTokenAndExpiry() {
        FileCategory category = FileCategory.builder().id(3L).categoryCode("ATTACHMENT").build();
        when(fileCategoryRepository.findById(3L)).thenReturn(Optional.of(category));

        FileUploadTokenRequest request = FileUploadTokenRequest.builder()
            .ownerId(1001L)
            .ownerType("PURCHASE_ORDER")
            .moduleCode("PRC")
            .fileCategoryFk(3L)
            .build();

        FileUploadTokenResponse response = service.issueUploadToken(request).getData();

        assertThat(response.getEncryptedToken()).isNotBlank();
        assertThat(response.getExpiresAt()).isNotNull();
    }

    @Test
    void issueUploadToken_unknownFileCategory_throwsNotFound() {
        when(fileCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        FileUploadTokenRequest request = FileUploadTokenRequest.builder()
            .ownerId(1001L)
            .ownerType("PURCHASE_ORDER")
            .moduleCode("PRC")
            .fileCategoryFk(99L)
            .build();

        assertThatThrownBy(() -> service.issueUploadToken(request))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_CATEGORY_NOT_FOUND));
    }
}
