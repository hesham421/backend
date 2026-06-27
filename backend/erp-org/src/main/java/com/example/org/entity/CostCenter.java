package com.example.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

// Tree entity — self-reference via PARENT_COST_CENTER_FK (NULLABLE)
@Entity
@Table(name = "ORG_COST_CENTER",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_CC_CODE_BR", columnNames = {"COST_CENTER_CODE", "BRANCH_FK"})
    },
    indexes = {
        @Index(name = "IDX_ORG_CC_BRANCH", columnList = "BRANCH_FK"),
        @Index(name = "IDX_ORG_CC_PARENT", columnList = "PARENT_COST_CENTER_FK"),
        @Index(name = "IDX_ORG_CC_ACTIVE", columnList = "IS_ACTIVE_FL"),
        @Index(name = "IDX_ORG_CC_NODE_TYPE", columnList = "NODE_TYPE_ID")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CostCenter extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cost_center_seq")
    @SequenceGenerator(name = "cost_center_seq", sequenceName = "SEQ_ORG_COST_CENTER", allocationSize = 1)
    @Column(name = "COST_CENTER_PK")
    private Long costCenterPk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "COST_CENTER_CODE", length = 20, nullable = false, updatable = false)
    private String costCenterCode;

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
    @JoinColumn(name = "BRANCH_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_CC_BR"))
    private Branch branch;

    // Self-reference: nullable for root nodes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_COST_CENTER_FK", nullable = true,
        foreignKey = @ForeignKey(name = "FK_ORG_CC_PARENT"))
    private CostCenter parentCostCenter;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "NODE_TYPE_ID", length = 50, nullable = false)
    private String nodeTypeId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "COST_CENTER_TYPE_ID", length = 50, nullable = false)
    private String costCenterTypeId;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

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
