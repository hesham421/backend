package com.example.erp.file.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class FileContentTypeDetectorTest {

    @Test
    void detect_pdfMagicBytes_returnsDocument() {
        byte[] content = "%PDF-1.4 rest of file".getBytes(StandardCharsets.UTF_8);

        DetectedFileType detected = FileContentTypeDetector.detect(content);

        assertThat(detected.mimeType()).isEqualTo("application/pdf");
        assertThat(detected.fileTypeId()).isEqualTo(FileContentTypeDetector.FILE_TYPE_DOCUMENT);
    }

    @Test
    void detect_pngMagicBytes_returnsImage() {
        byte[] content = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        DetectedFileType detected = FileContentTypeDetector.detect(content);

        assertThat(detected.mimeType()).isEqualTo("image/png");
        assertThat(detected.fileTypeId()).isEqualTo(FileContentTypeDetector.FILE_TYPE_IMAGE);
    }

    @Test
    void detect_zipWithWordEntry_returnsDocxDocument() throws IOException {
        byte[] content = zipWithEntry("word/document.xml");

        DetectedFileType detected = FileContentTypeDetector.detect(content);

        assertThat(detected.fileTypeId()).isEqualTo(FileContentTypeDetector.FILE_TYPE_DOCUMENT);
        assertThat(detected.mimeType()).contains("wordprocessingml");
    }

    @Test
    void detect_zipWithXlEntry_returnsSpreadsheet() throws IOException {
        byte[] content = zipWithEntry("xl/workbook.xml");

        DetectedFileType detected = FileContentTypeDetector.detect(content);

        assertThat(detected.fileTypeId()).isEqualTo(FileContentTypeDetector.FILE_TYPE_SPREADSHEET);
        assertThat(detected.mimeType()).contains("spreadsheetml");
    }

    @Test
    void detect_genericZip_returnsArchive() throws IOException {
        byte[] content = zipWithEntry("readme.txt");

        DetectedFileType detected = FileContentTypeDetector.detect(content);

        assertThat(detected.fileTypeId()).isEqualTo(FileContentTypeDetector.FILE_TYPE_ARCHIVE);
        assertThat(detected.mimeType()).isEqualTo("application/zip");
    }

    @Test
    void detect_unknownBytes_returnsOther() {
        byte[] content = "just plain text with no magic bytes".getBytes(StandardCharsets.UTF_8);

        DetectedFileType detected = FileContentTypeDetector.detect(content);

        assertThat(detected.fileTypeId()).isEqualTo(FileContentTypeDetector.FILE_TYPE_OTHER);
        assertThat(detected.mimeType()).isEqualTo("application/octet-stream");
    }

    @Test
    void detect_emptyContent_returnsOther() {
        DetectedFileType detected = FileContentTypeDetector.detect(new byte[0]);

        assertThat(detected.fileTypeId()).isEqualTo(FileContentTypeDetector.FILE_TYPE_OTHER);
    }

    private static byte[] zipWithEntry(String entryName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write("content".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
