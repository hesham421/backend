package com.erp.file.repository;

import com.erp.file.entity.FileDocument;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-FILE-001 (FileDocument). Metadata reads (QR-FILE-0004 single,
 * QR-FILE-0005 owner list) return the bytes-excluded {@link FileMetadataView} projection (DRV-003);
 * only the download path (QR-FILE-0003) loads the BYTEA content. Ownership is polymorphic with no
 * FK — the owner list matches ownerId/ownerType/moduleCode EXACT with optional fileTypeId/
 * fileStatusId EXACT. Module-internal; consumed only by the FILE services.
 */
@Repository
public interface FileDocumentRepository
    extends JpaRepository<FileDocument, Long>,
            JpaSpecificationExecutor<FileDocument> {

    String METADATA_SELECT = "SELECT f.id AS id, f.ownerId AS ownerId, f.ownerType AS ownerType, "
        + "f.moduleCode AS moduleCode, f.fileName AS fileName, f.contentType AS contentType, "
        + "f.fileSize AS fileSize, f.fileTypeId AS fileTypeId, f.fileStatusId AS fileStatusId, "
        + "f.fileCategoryFk.id AS fileCategoryId, f.createdAt AS createdAt, f.createdBy AS createdBy, "
        + "f.updatedAt AS updatedAt, f.updatedBy AS updatedBy FROM FileDocument f";

    /** QR-FILE-0004 / QR-FILE-0002 — single metadata read by id (bytes excluded). */
    @Query(METADATA_SELECT + " WHERE f.id = :id")
    Optional<FileMetadataView> findMetadataById(@Param("id") Long id);

    /**
     * QR-FILE-0005 — owner list (bytes excluded, DRV-003). ownerId/ownerType/moduleCode EXACT;
     * fileTypeId/fileStatusId optional EXACT (null param = no filter). Paged, no join.
     */
    @Query(value = METADATA_SELECT + " WHERE f.ownerId = :ownerId AND f.ownerType = :ownerType "
        + "AND f.moduleCode = :moduleCode "
        + "AND (:fileTypeId IS NULL OR f.fileTypeId = :fileTypeId) "
        + "AND (:fileStatusId IS NULL OR f.fileStatusId = :fileStatusId)",
        countQuery = "SELECT COUNT(f) FROM FileDocument f WHERE f.ownerId = :ownerId "
        + "AND f.ownerType = :ownerType AND f.moduleCode = :moduleCode "
        + "AND (:fileTypeId IS NULL OR f.fileTypeId = :fileTypeId) "
        + "AND (:fileStatusId IS NULL OR f.fileStatusId = :fileStatusId)")
    Page<FileMetadataView> findMetadataByOwner(@Param("ownerId") Long ownerId,
                                               @Param("ownerType") String ownerType,
                                               @Param("moduleCode") String moduleCode,
                                               @Param("fileTypeId") String fileTypeId,
                                               @Param("fileStatusId") String fileStatusId,
                                               Pageable pageable);

    /** QR-FILE-0003 — full row incl. BYTEA content, download path only. */
    @Query("SELECT f FROM FileDocument f WHERE f.id = :id")
    Optional<FileDocument> findWithContentById(@Param("id") Long id);
}
