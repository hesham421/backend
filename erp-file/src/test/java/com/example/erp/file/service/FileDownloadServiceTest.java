package com.example.erp.file.service;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.dto.FileDownloadResult;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileDocumentRepository;
import com.example.erp.file.security.FileTokenPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceTest {

    @Mock
    private FileDocumentRepository fileDocumentRepository;

    private FileDownloadService service;

    @BeforeEach
    void setUp() {
        service = new FileDownloadService(fileDocumentRepository);
    }

    @Test
    void getForDownload_activeFile_returnsContent() {
        FileDocument document = FileDocument.builder()
            .id(501L)
            .fileNameOriginal("invoice.pdf")
            .mimeType("application/pdf")
            .fileContent("content".getBytes(StandardCharsets.UTF_8))
            .fileStatusId(FileDocument.STATUS_ACTIVE)
            .build();
        when(fileDocumentRepository.findById(501L)).thenReturn(Optional.of(document));

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 501L);
        FileDownloadResult result = service.getForDownload(tokenPayload);

        assertThat(result.fileNameOriginal()).isEqualTo("invoice.pdf");
        assertThat(result.mimeType()).isEqualTo("application/pdf");
        assertThat(new String(result.content(), StandardCharsets.UTF_8)).isEqualTo("content");
    }

    @Test
    void getForDownload_deletedFile_throwsGone() {
        FileDocument document = FileDocument.builder()
            .id(501L)
            .fileStatusId(FileDocument.STATUS_DELETED)
            .build();
        when(fileDocumentRepository.findById(501L)).thenReturn(Optional.of(document));

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 501L);

        assertThatThrownBy(() -> service.getForDownload(tokenPayload))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> {
                LocalizedException localized = (LocalizedException) ex;
                assertThat(localized.getMessageKey()).isEqualTo(FileErrorCodes.FILE_NO_LONGER_AVAILABLE);
                assertThat(localized.getStatusCode()).isEqualTo(Status.GONE);
            });
    }

    @Test
    void getForDownload_unknownFile_throwsNotFound() {
        when(fileDocumentRepository.findById(99L)).thenReturn(Optional.empty());

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 99L);

        assertThatThrownBy(() -> service.getForDownload(tokenPayload))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_DOCUMENT_NOT_FOUND));
    }
}
