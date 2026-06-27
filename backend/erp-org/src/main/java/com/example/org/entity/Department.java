package com.example.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

// Tree entity — self-reference via PARENT_DEPARTMENT_FK (NULLABLE)
@Entity
@Table(name = "ORG_DEPARTMENT",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_DEP_CODE_BR", columnNames = {"DEPT_CODE", "BRANCH_FK"})
    },
    indexes = {
        @Index(name = "IDX_ORG_DEP_BRANCH", columnList = "BRANCH_FK"),
        @Index(name = "IDX_ORG_DEP_PARENT", columnList = "PARENT_DEPARTMENT_FK"),
        @Index(name = "IDX_ORG_DEP_ACTIVE", columnList = "IS_ACTIVE_FL"),
        @Index(name = "IDX_ORG_DEP_NODE_TYPE", columnList = "NODE_TYPE_ID")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Department extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department_seq")
    @SequenceGenerator(name = "department_seq", sequenceName = "SEQ_ORG_DEPARTMENT", allocationSize = 1)
    @Column(name = "DEPARTMENT_PK")
    private Long departmentPk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "DEPT_CODE", length = 20, nullable = false, updatable = false)
    private String deptCode;

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
        foreignKey = @ForeignKey(name = "FK_ORG_DEP_BR"))
    private Branch branch;

    // Self-reference: nullable for root nodes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_DEPARTMENT_FK", nullable = true,
        foreignKey = @ForeignKey(name = "FK_ORG_DEP_PARENT"))
    private Department parentDepartment;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "NODE_TYPE_ID", length = 50, nullable = false)
    private String nodeTypeId;

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
