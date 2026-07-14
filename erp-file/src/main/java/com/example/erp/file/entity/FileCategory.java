package com.example.erp.file.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity for FILE_CATEGORY (ENTITY-FILE-002, DBF-0001..0012). Reference Table
 * (module_code-scoped, &gt;15 values platform-wide — master-registry Lookup Governance Rule),
 * parent FK of FILE_DOCUMENT. Admin-managed Create/Read/Update/Deactivate. No domain/ package —
 * per CORE.md ("small, entity-local behaviors — no separate domain/ class package is warranted
 * for a 2-entity Foundation module. Consistent with DBS-ORG-001 precedent scale reasoning"),
 * same precedent as {@code OrgRegionType}.
 */
@Entity
@Table(name = "FILE_CATEGORY",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_FILE_CATEGORY_MODULE_CODE", columnNames = {"MODULE_CODE", "CATEGORY_CODE"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class FileCategory extends AuditableEntity {

    /** Platform default content size limit (bytes) — RULE-FILE-001, applied when no override is set. */
    public static final long DEFAULT_MAX_SIZE_BYTES = 5_242_880L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_category_seq")
    @SequenceGenerator(name = "file_category_seq", sequenceName = "SEQ_FILE_CATEGORY", allocationSize = 1)
    @Column(name = "FILE_CATEGORY_PK")
    private Long id;

    // DBF-0002 — immutable after creation, unique within MODULE_CODE (lookupKey-like pattern, not a BC-RULE-2 Business Code)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "CATEGORY_CODE", length = 50, nullable = false)
    private String categoryCode;

    // DBF-0003 — free text, owning module (e.g. NOTIFICATION, PRC)
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "MODULE_CODE", length = 20, nullable = false)
    private String moduleCode;

    // DBF-0004
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0005
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0006 — optional override of DEFAULT_MAX_SIZE_BYTES, consumed by RULE-FILE-001 at upload time
    @Column(name = "MAX_SIZE_BYTES_OVERRIDE")
    private Long maxSizeBytesOverride;

    // DBF-0007 — advisory only, not enforced
    @Size(max = 500, message = "{validation.size}")
    @Column(name = "ALLOWED_TYPES_NOTE", length = 500)
    private String allowedTypesNote;

    // DBF-0008
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (categoryCode != null) {
            categoryCode = categoryCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (categoryCode != null) {
            categoryCode = categoryCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }

    /**
     * RULE-FILE-001 — returns this category's size override when set, else the platform default
     * (5MB). Entity-embedded per CORE.md; a pure fallback-resolution accessor, not a Business
     * Decision ("is this allowed") requiring a Domain object.
     */
    public long resolveMaxSizeBytes() {
        return maxSizeBytesOverride != null ? maxSizeBytesOverride : DEFAULT_MAX_SIZE_BYTES;
    }
}
