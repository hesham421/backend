package com.example.erp.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
 * JPA entity for ORG_LEGAL_ENTITY (ENTITY-ORG-001, DBF-0001..0011). ROOT entity of the
 * Organization module. Persistence-only — deactivation guards (RULE-ORG-001, RULE-ORG-002)
 * live on {@link com.example.erp.org.domain.OrgLegalEntityDomain}.
 */
@Entity
@Table(name = "ORG_LEGAL_ENTITY",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_LE_CODE", columnNames = {"LEGAL_ENTITY_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_LE_ENTITY_TYPE", columnList = "ENTITY_TYPE_ID"),
        @Index(name = "IDX_ORG_LE_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgLegalEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_legal_entity_seq")
    @SequenceGenerator(name = "org_legal_entity_seq", sequenceName = "ORG_LEGAL_ENTITY_SEQ", allocationSize = 1)
    @Column(name = "LEGAL_ENTITY_PK")
    private Long id;

    // DBF-0002 — Business code LE-NNNNN. NumberingEngine only (RULE-ORG-013). Immutable (RULE-ORG-011/014).
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "LEGAL_ENTITY_CODE", length = 20, nullable = false)
    private String legalEntityCode;

    // DBF-0003 — Unique within global scope (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0004 — Unique within global scope (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0005 — LOV code string (LOV-ORG-001: HEAD_OFFICE, BRANCH_OFFICE, SUBSIDIARY, REPRESENTATIVE_OFFICE)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "ENTITY_TYPE_ID", length = 50, nullable = false)
    private String entityTypeId;

    // DBF-0006 — Deactivation governed by RULE-ORG-001 (active Branches), RULE-ORG-002 (active ProfitCenters)
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    // DBF-0007
    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (legalEntityCode != null) {
            legalEntityCode = legalEntityCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (legalEntityCode != null) {
            legalEntityCode = legalEntityCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
