package com.example.erp.file.service;

import com.example.erp.file.dto.FileDocumentSummaryResponse;
import com.example.erp.file.entity.FileCategory;
import com.example.erp.file.entity.FileDocument;
import com.example.erp.file.repository.FileDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileListServiceTest {

    @Mock
    private FileDocumentRepository fileDocumentRepository;

    private FileListService service;

    @BeforeEach
    void setUp() {
        service = new FileListService(fileDocumentRepository);
    }

    @Test
    void listByOwner_returnsMappedSummaries_neverIncludingFileContent() {
        FileCategory category = FileCategory.builder().id(3L).nameAr("مرفقات").nameEn("Attachments").build();
        FileDocument document = FileDocument.builder()
            .id(501L)
            .fileNameOriginal("invoice.pdf")
            .fileCategory(category)
            .fileTypeId("DOCUMENT")
            .fileSizeBytes(2048L)
            .fileStatusId(FileDocument.STATUS_ACTIVE)
            .fileContent("should never leak".getBytes())
            .build();
        Page<FileDocument> page = new PageImpl<>(List.of(document));
        when(fileDocumentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<FileDocumentSummaryResponse> result =
            service.listByOwner(1001L, "PURCHASE_ORDER", 0, 20, null, null).getData();

        assertThat(result.getContent()).hasSize(1);
        FileDocumentSummaryResponse summary = result.getContent().get(0);
        assertThat(summary.getFileDocumentPk()).isEqualTo(501L);
        assertThat(summary.getFileNameOriginal()).isEqualTo("invoice.pdf");
        assertThat(summary.getFileCategoryNameAr()).isEqualTo("مرفقات");
        assertThat(summary.getFileCategoryNameEn()).isEqualTo("Attachments");
    }

    @Test
    void listByOwner_appliesOwnerIdFilter() {
        Page<FileDocument> page = new PageImpl<>(List.of());
        ArgumentCaptor<Specification<FileDocument>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(fileDocumentRepository.findAll(specCaptor.capture(), any(Pageable.class))).thenReturn(page);

        service.listByOwner(1001L, null, 0, 20, null, null);

        assertThat(specCaptor.getValue()).isNotNull();
        verify(fileDocumentRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
