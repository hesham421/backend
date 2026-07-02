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
 * JPA entity for ORG_BRANCH (ENTITY-ORG-002, DBF-0012..0023). Primary DataScope boundary.
 * Persistence-only — deactivation guards (RULE-ORG-003/004/005) and the parent-active create
 * guard (RULE-ORG-018) live on {@link com.example.erp.org.domain.OrgBranchDomain}.
 */
@Entity
@Table(name = "ORG_BRANCH",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_BR_CODE_LE", columnNames = {"LEGAL_ENTITY_FK", "BRANCH_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_BR_LE_FK", columnList = "LEGAL_ENTITY_FK"),
        @Index(name = "IDX_ORG_BR_IS_ACTIVE", columnList = "IS_ACTIVE_FL"),
        @Index(name = "IDX_ORG_BR_TYPE", columnList = "BRANCH_TYPE_ID")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgBranch extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_branch_seq")
    @SequenceGenerator(name = "org_branch_seq", sequenceName = "ORG_BRANCH_SEQ", allocationSize = 1)
    @Column(name = "BRANCH_PK")
    private Long id;

    // DBF-0013 — Business code BR-[LE_CODE]-NNNNN. NumberingEngine only (RULE-ORG-013). Immutable (RULE-ORG-011/014).
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "BRANCH_CODE", length = 20, nullable = false)
    private String branchCode;

    // DBF-0014 — Unique within same LegalEntity (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0015 — Unique within same LegalEntity (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0016 — Creation blocked if parent LegalEntity inactive (RULE-ORG-018)
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGAL_ENTITY_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_BR_LE"))
    private OrgLegalEntity legalEntity;

    // DBF-0017 — LOV code string (LOV-ORG-002: MAIN_BRANCH, SUB_BRANCH, OPERATIONS_BRANCH, ADMIN_BRANCH)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "BRANCH_TYPE_ID", length = 50, nullable = false)
    private String branchTypeId;

    // DBF-0018 — Deactivation governed by RULE-ORG-003 (Departments), RULE-ORG-004 (CostCenters), RULE-ORG-005 (LocationSites)
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    // DBF-0019
    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (branchCode != null) {
            branchCode = branchCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (branchCode != null) {
            branchCode = branchCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
