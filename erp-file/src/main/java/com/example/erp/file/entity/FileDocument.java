package com.example.erp.file.entity;

import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity for FILE_DOCUMENT (ENTITY-FILE-001, DBF-0013..0027). SHARED (owner) — central
 * binary file storage for all platform modules. No Update operation — replacement is
 * delete+re-upload. No domain/ package — per CORE.md, {@code purgeContent()} below is a small,
 * entity-local behavior, same reasoning as {@code FileCategory.resolveMaxSizeBytes()}.
 */
@Entity
@Table(name = "FILE_DOCUMENT",
    indexes = {
        @Index(name = "IDX_FILE_DOCUMENT_FILE_CATEGORY_FK", columnList = "FILE_CATEGORY_FK"),
        @Index(name = "IDX_FILE_DOCUMENT_OWNER", columnList = "OWNER_ID, OWNER_TYPE")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class FileDocument extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_document_seq")
    @SequenceGenerator(name = "file_document_seq", sequenceName = "SEQ_FILE_DOCUMENT", allocationSize = 1)
    @Column(name = "FILE_DOCUMENT_PK")
    private Long id;

    // DBF-0014 — polymorphic reference, no physical FK (target table determined by ownerType — ADAPT-05)
    @NotNull(message = "{validation.required}")
    @Column(name = "OWNER_ID", nullable = false)
    private Long ownerId;

    // DBF-0015 — free text, NOT a governed LOV (same pattern as NOTIF_LOG.reference_type)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "OWNER_TYPE", length = 100, nullable = false)
    private String ownerType;

    // DBF-0016 — producing module code
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "MODULE_CODE", length = 20, nullable = false)
    private String moduleCode;

    // DBF-0017 — intra-module FK, exact column name per db-script.md (not the generic _ID_FK suffix — same precedent as OrgRegion.LEGAL_ENTITY_FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FILE_CATEGORY_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_FILE_DOCUMENT_FILE_CATEGORY"))
    private FileCategory fileCategory;

    // DBF-0018 — LOV-FILE-001, auto-detected from content (RULE-FILE-005), never from client Content-Type
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "FILE_TYPE_ID", length = 50, nullable = false)
    private String fileTypeId;

    // DBF-0019 — as uploaded by user
    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.size}")
    @Column(name = "FILE_NAME_ORIGINAL", length = 255, nullable = false)
    private String fileNameOriginal;

    // DBF-0020 — detected server-side from content, never trusts client Content-Type header (RULE-FILE-005)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "MIME_TYPE", length = 100, nullable = false)
    private String mimeType;

    // DBF-0021 — computed at upload time
    @NotNull(message = "{validation.required}")
    @Column(name = "FILE_SIZE_BYTES", nullable = false)
    private Long fileSizeBytes;

    // DBF-0022 — BYTEA, extends CORE-8 (RESOLUTION-01). Never in list/search DTOs; streamed on
    // download. Required at upload (enforced by the create-request DTO, not a DB constraint) but
    // deliberately NULLABLE here: db-script.md's own DDL declares this column NOT NULL while its
    // column comment simultaneously documents RULE-FILE-006 setting it to NULL at app layer on
    // delete — those two statements contradict each other, and OQ-001's resolution (row retained,
    // content purged) is the one the rest of the plan actually depends on, so nullable wins. See
    // DATAOM handoff for the flagged discrepancy.
    @Column(name = "FILE_CONTENT")
    private byte[] fileContent;

    // DBF-0023 — LOV-FILE-002, Status Lifecycle (ACTIVE/ARCHIVED/DELETED — A6), not a simple isActiveFl flag
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "FILE_STATUS_ID", length = 50, nullable = false)
    private String fileStatusId;

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_ARCHIVED = "ARCHIVED";
    public static final String STATUS_DELETED = "DELETED";

    public void archive() {
        this.fileStatusId = STATUS_ARCHIVED;
    }

    /**
     * RULE-FILE-006 — permanent, irreversible content purge. Sets fileContent = null and
     * fileStatusId = DELETED; the FILE_DOCUMENT row itself is retained (metadata + audit trail),
     * per OQ-001's resolution, so any consumer HARD-FK stays valid. Called only from the Service
     * delete flow after RULE-FILE-007's ownership/Admin check (an authorization concern, resolved
     * against Security's role/permission model, not an entity-local decision) has already passed.
     */
    public void purgeContent() {
        this.fileContent = null;
        this.fileStatusId = STATUS_DELETED;
    }
}
