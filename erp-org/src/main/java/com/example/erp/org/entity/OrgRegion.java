package com.example.erp.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity for ORG_REGION (ENTITY-ORG-003, DBF-0033..0044). Persistence-only.
 *
 * TODO: OQ-001 — RULE-ORG-006 (block deactivation while active Branches reference this Region)
 * is pending resolution: db-script.md's DBF matrix has no REGION_FK column on ORG_BRANCH, so the
 * guard cannot be implemented against the current schema. Not enforced until the FK linkage is
 * confirmed at a MODE 1.5 amendment. RULE-ORG-017 (SOFT-READ consumer warning) is non-blocking
 * and surfaced at the UI layer only — no Domain guard required.
 */
@Entity
@Table(name = "ORG_REGION",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_RG_CODE_LE", columnNames = {"LEGAL_ENTITY_FK", "REGION_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_RG_LE_FK", columnList = "LEGAL_ENTITY_FK"),
        @Index(name = "IDX_ORG_RG_RT_FK", columnList = "REGION_TYPE_ID_FK"),
        @Index(name = "IDX_ORG_RG_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgRegion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_region_seq")
    @SequenceGenerator(name = "org_region_seq", sequenceName = "ORG_REGION_SEQ", allocationSize = 1)
    @Column(name = "REGION_PK")
    private Long id;

    // DBF-0034 — Business code RG-[LE_CODE]-NNNNN. NumberingEngine only (RULE-ORG-013). Immutable (RULE-ORG-011/014).
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "REGION_CODE", length = 20, nullable = false)
    private String regionCode;

    // DBF-0035 — Unique within same LegalEntity (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0036 — Unique within same LegalEntity (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0037
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGAL_ENTITY_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_RG_LE"))
    private OrgLegalEntity legalEntity;

    // DBF-0038
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_TYPE_ID_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_RG_RT"))
    private OrgRegionType regionType;

    // DBF-0039 — Deactivation guard RULE-ORG-006 OQ-001 DEFERRED; RULE-ORG-017 non-blocking (UI layer only)
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    // DBF-0040
    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (regionCode != null) {
            regionCode = regionCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (regionCode != null) {
            regionCode = regionCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
