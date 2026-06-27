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
@Table(name = "ORG_LEGAL_ENTITY",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_LE_CODE", columnNames = {"LEGAL_ENTITY_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_LE_ACTIVE", columnList = "IS_ACTIVE_FL"),
        @Index(name = "IDX_ORG_LE_TYPE", columnList = "ENTITY_TYPE_ID")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LegalEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "legal_entity_seq")
    @SequenceGenerator(name = "legal_entity_seq", sequenceName = "SEQ_ORG_LEGAL_ENTITY", allocationSize = 1)
    @Column(name = "LEGAL_ENTITY_PK")
    private Long legalEntityPk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "LEGAL_ENTITY_CODE", length = 20, nullable = false, updatable = false)
    private String legalEntityCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "ENTITY_TYPE_ID", length = 50, nullable = false)
    private String entityTypeId;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @Formula("(SELECT COUNT(*) FROM ORG_BRANCH b WHERE b.LEGAL_ENTITY_FK = LEGAL_ENTITY_PK AND b.IS_ACTIVE_FL = 1)")
    private Integer activeBranchCount;

    @Formula("(SELECT COUNT(*) FROM ORG_PROFIT_CENTER pc WHERE pc.LEGAL_ENTITY_FK = LEGAL_ENTITY_PK AND pc.IS_ACTIVE_FL = 1)")
    private Integer activeProfitCenterCount;

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
