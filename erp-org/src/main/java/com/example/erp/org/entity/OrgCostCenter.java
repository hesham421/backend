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
 * JPA entity for ORG_COST_CENTER (ENTITY-ORG-005, DBF-0058..0071). Hierarchical tree per Branch.
 * Persistence-only — creation guard (RULE-ORG-019) and cycle prevention (RULE-ORG-008, mirrors
 * RULE-ORG-007) live on {@link com.example.erp.org.domain.OrgCostCenterDomain}. RULE-ORG-010
 * (SUMMARY nodes blocked on transactional records) is enforced by consuming modules, not here —
 * informational only.
 */
@Entity
@Table(name = "ORG_COST_CENTER",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_CC_CODE_BR", columnNames = {"BRANCH_FK", "COST_CENTER_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_CC_BR_FK", columnList = "BRANCH_FK"),
        @Index(name = "IDX_ORG_CC_PARENT_FK", columnList = "PARENT_COST_CENTER_FK"),
        @Index(name = "IDX_ORG_CC_NODE_TYPE", columnList = "NODE_TYPE_ID"),
        @Index(name = "IDX_ORG_CC_TYPE", columnList = "COST_CENTER_TYPE_ID"),
        @Index(name = "IDX_ORG_CC_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgCostCenter extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_cost_center_seq")
    @SequenceGenerator(name = "org_cost_center_seq", sequenceName = "ORG_COST_CENTER_SEQ", allocationSize = 1)
    @Column(name = "COST_CENTER_PK")
    private Long id;

    // DBF-0059 — Business code CC-[BR_CODE]-NNNNN. NumberingEngine only (RULE-ORG-013). Immutable (RULE-ORG-011/014).
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "COST_CENTER_CODE", length = 20, nullable = false)
    private String costCenterCode;

    // DBF-0060 — Unique within same Branch (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0061 — Unique within same Branch (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0062 — Creation blocked if parent Branch inactive (RULE-ORG-019)
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_CC_BR"))
    private OrgBranch branch;

    // DBF-0063 — Self-reference, NULLABLE (root nodes have no parent). Cycle prevention RULE-ORG-008.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_COST_CENTER_FK",
        foreignKey = @ForeignKey(name = "FK_ORG_CC_PARENT"))
    private OrgCostCenter parentCostCenter;

    // DBF-0064 — LOV code string (LOV-ORG-004: SUMMARY, DETAIL). Immutable after first save (RULE-ORG-020).
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "NODE_TYPE_ID", length = 50, nullable = false)
    private String nodeTypeId;

    // DBF-0065 — LOV code string (LOV-ORG-005: DIRECT, INDIRECT, SHARED)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "COST_CENTER_TYPE_ID", length = 50, nullable = false)
    private String costCenterTypeId;

    // DBF-0066
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    // DBF-0067
    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (costCenterCode != null) {
            costCenterCode = costCenterCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (costCenterCode != null) {
            costCenterCode = costCenterCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
