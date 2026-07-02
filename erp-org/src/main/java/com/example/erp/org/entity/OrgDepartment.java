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
 * JPA entity for ORG_DEPARTMENT (ENTITY-ORG-004, DBF-0045..0057). Hierarchical tree per Branch.
 * Persistence-only — creation guard (RULE-ORG-019) and cycle prevention (RULE-ORG-007) live on
 * {@link com.example.erp.org.domain.OrgDepartmentDomain}. RULE-ORG-009 (SUMMARY nodes blocked on
 * transactional records) is enforced by consuming modules, not here — informational only.
 */
@Entity
@Table(name = "ORG_DEPARTMENT",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_DEP_CODE_BR", columnNames = {"BRANCH_FK", "DEPARTMENT_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_DEP_BR_FK", columnList = "BRANCH_FK"),
        @Index(name = "IDX_ORG_DEP_PARENT_FK", columnList = "PARENT_DEPARTMENT_FK"),
        @Index(name = "IDX_ORG_DEP_NODE_TYPE", columnList = "NODE_TYPE_ID"),
        @Index(name = "IDX_ORG_DEP_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgDepartment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_department_seq")
    @SequenceGenerator(name = "org_department_seq", sequenceName = "ORG_DEPARTMENT_SEQ", allocationSize = 1)
    @Column(name = "DEPARTMENT_PK")
    private Long id;

    // DBF-0046 — Business code DEP-[BR_CODE]-NNNNN. NumberingEngine only (RULE-ORG-013). Immutable (RULE-ORG-011/014).
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "DEPARTMENT_CODE", length = 20, nullable = false)
    private String departmentCode;

    // DBF-0047 — Unique within same Branch (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0048 — Unique within same Branch (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0049 — Creation blocked if parent Branch inactive (RULE-ORG-019)
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_DEP_BR"))
    private OrgBranch branch;

    // DBF-0050 — Self-reference, NULLABLE (root nodes have no parent). Cycle prevention RULE-ORG-007.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_DEPARTMENT_FK",
        foreignKey = @ForeignKey(name = "FK_ORG_DEP_PARENT"))
    private OrgDepartment parentDepartment;

    // DBF-0051 — LOV code string (LOV-ORG-003: SUMMARY, DETAIL). Immutable after first save (RULE-ORG-020).
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "NODE_TYPE_ID", length = 50, nullable = false)
    private String nodeTypeId;

    // DBF-0052
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    // DBF-0053
    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (departmentCode != null) {
            departmentCode = departmentCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (departmentCode != null) {
            departmentCode = departmentCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
