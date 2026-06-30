package com.example.org.domain;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORG_COST_CENTER",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ORG_CC_CODE_BR", columnNames = {"BRANCH_FK", "COST_CENTER_CODE"})
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

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "COST_CENTER_CODE", length = 20, nullable = false)
    private String costCenterCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // RULE-ORG-019: parent Branch must be active at create time
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_CC_BR"))
    private OrgBranch branch;

    // RULE-ORG-008: cycle prevention — walk ancestor chain on assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_COST_CENTER_FK",
        foreignKey = @ForeignKey(name = "FK_ORG_CC_PARENT"))
    private OrgCostCenter parentCostCenter;

    @OneToMany(mappedBy = "parentCostCenter", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrgCostCenter> children = new ArrayList<>();

    // LOV-ORG-004: SUMMARY, DETAIL — immutable after first save (RULE-ORG-020)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "NODE_TYPE_ID", length = 50, nullable = false)
    private String nodeTypeId;

    // LOV-ORG-005: DIRECT, INDIRECT, SHARED
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
        if (isActiveFl == null) isActiveFl = Boolean.TRUE;
    }

    public void activate() { this.isActiveFl = Boolean.TRUE; }
    public void deactivate() { this.isActiveFl = Boolean.FALSE; }
}
