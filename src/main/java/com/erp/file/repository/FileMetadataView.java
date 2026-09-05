package com.erp.file.repository;

import java.time.Instant;

/**
 * Read-only interface projection for FileDocument metadata (DRV-003) — every column EXCEPT the
 * BYTEA {@code fileContent}. Backs the single metadata read (QR-FILE-0004) and the paged owner
 * list (QR-FILE-0005) so those reads never load file bytes. {@code fileCategoryId} reads the
 * FILE_CATEGORY_FK column via the to-one identifier (no join — Hibernate resolves it from the FK).
 */
public interface FileMetadataView {

    Long getId();

    Long getOwnerId();

    String getOwnerType();

    String getModuleCode();

    String getFileName();

    String getContentType();

    Long getFileSize();

    String getFileTypeId();

    String getFileStatusId();

    Long getFileCategoryId();

    Instant getCreatedAt();

    String getCreatedBy();

    Instant getUpdatedAt();

    String getUpdatedBy();
}
