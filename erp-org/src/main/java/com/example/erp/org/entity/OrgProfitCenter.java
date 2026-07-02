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
 * JPA entity for ORG_PROFIT_CENTER (ENTITY-ORG-006, DBF-0072..0082). Persistence-only. No
 * internal dependency guard on deactivate (per SRS A6 lifecycle table) and no parent-active
 * create guard — only the standard RULE-ORG-011..016 set applies, all of which are handled at
 * the DTO/Mapper/Service/Repository layers (code immutability, uniqueness). No Domain companion
 * is required — see {@code create-entity} skill, "pure reference/lookup table" exemption
 * (same reasoning applies here: no RULE-ID requires a Domain-level "is this allowed" decision).
 */
@Entity
@Table(name = "ORG_PROFIT_CENTER",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_PC_CODE_LE", columnNames = {"LEGAL_ENTITY_FK", "PROFIT_CENTER_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_PC_LE_FK", columnList = "LEGAL_ENTITY_FK"),
        @Index(name = "IDX_ORG_PC_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgProfitCenter extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_profit_center_seq")
    @SequenceGenerator(name = "org_profit_center_seq", sequenceName = "ORG_PROFIT_CENTER_SEQ", allocationSize = 1)
    @Column(name = "PROFIT_CENTER_PK")
    private Long id;

    // DBF-0073 — Business code PC-[LE_CODE]-NNNNN. NumberingEngine only (RULE-ORG-013). Immutable (RULE-ORG-011/014).
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "PROFIT_CENTER_CODE", length = 20, nullable = false)
    private String profitCenterCode;

    // DBF-0074 — Unique within same LegalEntity (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0075 — Unique within same LegalEntity (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0076
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGAL_ENTITY_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_PC_LE"))
    private OrgLegalEntity legalEntity;

    // DBF-0077 — No internal deactivation constraints (RULE-ORG-002 applies at LegalEntity level, not here)
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    // DBF-0078
    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (profitCenterCode != null) {
            profitCenterCode = profitCenterCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (profitCenterCode != null) {
            profitCenterCode = profitCenterCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
