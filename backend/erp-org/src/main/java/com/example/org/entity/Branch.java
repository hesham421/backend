package com.example.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "ORG_BRANCH",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_BR_CODE_LE", columnNames = {"BRANCH_CODE", "LEGAL_ENTITY_FK"})
    },
    indexes = {
        @Index(name = "IDX_ORG_BR_LEGAL_ENTITY", columnList = "LEGAL_ENTITY_FK"),
        @Index(name = "IDX_ORG_BR_ACTIVE", columnList = "IS_ACTIVE_FL"),
        @Index(name = "IDX_ORG_BR_TYPE", columnList = "BRANCH_TYPE_ID")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Branch extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "branch_seq")
    @SequenceGenerator(name = "branch_seq", sequenceName = "SEQ_ORG_BRANCH", allocationSize = 1)
    @Column(name = "BRANCH_PK")
    private Long branchPk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "BRANCH_CODE", length = 20, nullable = false, updatable = false)
    private String branchCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGAL_ENTITY_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_BR_LE"))
    private LegalEntity legalEntity;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "BRANCH_TYPE_ID", length = 50, nullable = false)
    private String branchTypeId;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @Formula("(SELECT COUNT(*) FROM ORG_DEPARTMENT d WHERE d.BRANCH_FK = BRANCH_PK AND d.IS_ACTIVE_FL = 1)")
    private Integer activeDepartmentCount;

    @Formula("(SELECT COUNT(*) FROM ORG_COST_CENTER cc WHERE cc.BRANCH_FK = BRANCH_PK AND cc.IS_ACTIVE_FL = 1)")
    private Integer activeCostCenterCount;

    @Formula("(SELECT COUNT(*) FROM ORG_LOCATION_SITE ls WHERE ls.BRANCH_FK = BRANCH_PK AND ls.IS_ACTIVE_FL = 1)")
    private Integer activeLocationSiteCount;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
