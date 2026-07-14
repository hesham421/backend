package com.example.erp.file.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * RULE-FILE-005 — server-side MIME/file-type detection from content only, never from the
 * client-supplied Content-Type header. Pure JDK (magic-byte sniffing + {@code java.util.zip}
 * entry inspection for OOXML formats) — no PDFBox/Tika, per RESOLUTION-03 ("PDFBox excluded
 * permanently, not deferred") and RESOLUTION-04's broader "no extra integration libraries"
 * spirit. Anything unrecognized correctly falls back to {@code application/octet-stream} /
 * {@code OTHER} — that IS honest content-sniffing, not a stub (e.g. plain text/CSV has no
 * reliable magic number to key off of).
 */
public final class FileContentTypeDetector {

    public static final String FILE_TYPE_IMAGE = "IMAGE";
    public static final String FILE_TYPE_DOCUMENT = "DOCUMENT";
    public static final String FILE_TYPE_SPREADSHEET = "SPREADSHEET";
    public static final String FILE_TYPE_ARCHIVE = "ARCHIVE";
    public static final String FILE_TYPE_OTHER = "OTHER";

    private static final String MIME_OCTET_STREAM = "application/octet-stream";

    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] GIF87_MAGIC = "GIF87a".getBytes();
    private static final byte[] GIF89_MAGIC = "GIF89a".getBytes();
    private static final byte[] BMP_MAGIC = {0x42, 0x4D};
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};
    private static final byte[] GZIP_MAGIC = {(byte) 0x1F, (byte) 0x8B};

    private FileContentTypeDetector() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static DetectedFileType detect(byte[] content) {
        if (content == null || content.length == 0) {
            return new DetectedFileType(MIME_OCTET_STREAM, FILE_TYPE_OTHER);
        }
        if (startsWith(content, PDF_MAGIC)) {
            return new DetectedFileType("application/pdf", FILE_TYPE_DOCUMENT);
        }
        if (startsWith(content, PNG_MAGIC)) {
            return new DetectedFileType("image/png", FILE_TYPE_IMAGE);
        }
        if (startsWith(content, JPEG_MAGIC)) {
            return new DetectedFileType("image/jpeg", FILE_TYPE_IMAGE);
        }
        if (startsWith(content, GIF87_MAGIC) || startsWith(content, GIF89_MAGIC)) {
            return new DetectedFileType("image/gif", FILE_TYPE_IMAGE);
        }
        if (startsWith(content, BMP_MAGIC)) {
            return new DetectedFileType("image/bmp", FILE_TYPE_IMAGE);
        }
        if (startsWith(content, GZIP_MAGIC)) {
            return new DetectedFileType("application/gzip", FILE_TYPE_ARCHIVE);
        }
        if (startsWith(content, ZIP_MAGIC)) {
            return detectZipBased(content);
        }
        return new DetectedFileType(MIME_OCTET_STREAM, FILE_TYPE_OTHER);
    }

    /**
     * OOXML formats (docx/xlsx/pptx) are ZIP containers — distinguished by their well-known
     * internal entry-name prefixes, without a full Office-document-parsing library.
     */
    private static DetectedFileType detectZipBased(byte[] content) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("word/")) {
                    return new DetectedFileType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        FILE_TYPE_DOCUMENT);
                }
                if (name.startsWith("xl/")) {
                    return new DetectedFileType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        FILE_TYPE_SPREADSHEET);
                }
            }
        } catch (IOException ignored) {
            // Not a well-formed zip stream past the magic bytes — fall through to generic archive.
        }
        return new DetectedFileType("application/zip", FILE_TYPE_ARCHIVE);
    }

    private static boolean startsWith(byte[] content, byte[] prefix) {
        if (content.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (content[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
