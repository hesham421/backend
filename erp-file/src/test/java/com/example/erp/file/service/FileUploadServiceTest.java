package com.example.erp.file.service;

import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.dto.FileUploadResponse;
import com.example.erp.file.entity.FileCategory;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileCategoryRepository;
import com.example.erp.file.repository.FileDocumentRepository;
import com.example.erp.file.security.FileTokenPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    @Mock
    private FileCategoryRepository fileCategoryRepository;
    @Mock
    private FileDocumentRepository fileDocumentRepository;

    private FileUploadService service;

    @BeforeEach
    void setUp() {
        service = new FileUploadService(fileCategoryRepository, fileDocumentRepository);
    }

    @Test
    void upload_pdfWithinLimit_persistsAndReturnsResponse() {
        FileCategory category = FileCategory.builder().id(3L).categoryCode("ATTACHMENT").build();
        when(fileCategoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(fileDocumentRepository.save(any(FileDocument.class))).thenAnswer(inv -> {
            FileDocument doc = inv.getArgument(0);
            doc.setId(501L);
            return doc;
        });

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 3L);
        MockMultipartFile file = new MockMultipartFile("file", "invoice.pdf", "application/pdf",
            "%PDF-1.4 fake content".getBytes(StandardCharsets.UTF_8));

        FileUploadResponse response = service.upload(tokenPayload, file).getData();

        assertThat(response.getFileDocumentPk()).isEqualTo(501L);
        assertThat(response.getFileNameOriginal()).isEqualTo("invoice.pdf");
        assertThat(response.getFileTypeId()).isEqualTo("DOCUMENT");
        assertThat(response.getFileStatusId()).isEqualTo(FileDocument.STATUS_ACTIVE);
    }

    @Test
    void upload_exceedsCategoryMaxSize_throwsBadRequest() {
        FileCategory category = FileCategory.builder().id(3L).categoryCode("ATTACHMENT")
            .maxSizeBytesOverride(10L).build();
        when(fileCategoryRepository.findById(3L)).thenReturn(Optional.of(category));

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 3L);
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf",
            "%PDF-1.4 this content is longer than ten bytes".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.upload(tokenPayload, file))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_SIZE_EXCEEDED));
    }

    @Test
    void upload_unknownFileCategory_throwsNotFound() {
        when(fileCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 99L);
        MockMultipartFile file = new MockMultipartFile("file", "invoice.pdf", "application/pdf",
            "content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.upload(tokenPayload, file))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_CATEGORY_NOT_FOUND));
    }

    @Test
    void upload_blankOriginalFilename_throwsBadRequest() {
        FileCategory category = FileCategory.builder().id(3L).categoryCode("ATTACHMENT").build();
        when(fileCategoryRepository.findById(3L)).thenReturn(Optional.of(category));

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 3L);
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf",
            "content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.upload(tokenPayload, file))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_NAME_REQUIRED));
    }
}
