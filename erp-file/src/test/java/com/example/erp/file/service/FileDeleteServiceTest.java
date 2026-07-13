package com.example.erp.file.service;

import com.example.erp.common.exception.LocalizedException;
import com.example.erp.file.dto.FileDeleteConfirmation;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.exception.FileErrorCodes;
import com.example.erp.file.repository.FileDocumentRepository;
import com.example.erp.file.security.FileTokenPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDeleteServiceTest {

    @Mock
    private FileDocumentRepository fileDocumentRepository;

    private FileDeleteService service;

    @BeforeEach
    void setUp() {
        service = new FileDeleteService(fileDocumentRepository);
    }

    @Test
    void delete_activeFile_purgesContentAndReturnsConfirmation() {
        FileDocument document = FileDocument.builder()
            .id(501L)
            .fileContent("secret content".getBytes(StandardCharsets.UTF_8))
            .fileStatusId(FileDocument.STATUS_ACTIVE)
            .build();
        when(fileDocumentRepository.findById(501L)).thenReturn(Optional.of(document));
        when(fileDocumentRepository.save(any(FileDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 501L);
        FileDeleteConfirmation confirmation = service.delete(tokenPayload).getData();

        assertThat(confirmation.getFileDocumentPk()).isEqualTo(501L);
        assertThat(confirmation.getFileStatusId()).isEqualTo(FileDocument.STATUS_DELETED);

        ArgumentCaptor<FileDocument> captor = ArgumentCaptor.forClass(FileDocument.class);
        verify(fileDocumentRepository).save(captor.capture());
        assertThat(captor.getValue().getFileContent()).isNull();
        assertThat(captor.getValue().getFileStatusId()).isEqualTo(FileDocument.STATUS_DELETED);
    }

    @Test
    void delete_unknownFile_throwsNotFound() {
        when(fileDocumentRepository.findById(99L)).thenReturn(Optional.empty());

        FileTokenPayload tokenPayload = new FileTokenPayload(1001L, "PURCHASE_ORDER", "PRC", 99L);

        assertThatThrownBy(() -> service.delete(tokenPayload))
            .isInstanceOf(LocalizedException.class)
            .satisfies(ex -> assertThat(((LocalizedException) ex).getMessageKey())
                .isEqualTo(FileErrorCodes.FILE_DOCUMENT_NOT_FOUND));
    }
}
