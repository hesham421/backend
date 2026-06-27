package com.example.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ORG_PROFIT_CENTER",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_PC_CODE_LE", columnNames = {"PROFIT_CENTER_CODE", "LEGAL_ENTITY_FK"})
    },
    indexes = {
        @Index(name = "IDX_ORG_PC_LEGAL_ENTITY", columnList = "LEGAL_ENTITY_FK"),
        @Index(name = "IDX_ORG_PC_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfitCenter extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profit_center_seq")
    @SequenceGenerator(name = "profit_center_seq", sequenceName = "SEQ_ORG_PROFIT_CENTER", allocationSize = 1)
    @Column(name = "PROFIT_CENTER_PK")
    private Long profitCenterPk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "PROFIT_CENTER_CODE", length = 20, nullable = false, updatable = false)
    private String profitCenterCode;

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
        foreignKey = @ForeignKey(name = "FK_ORG_PC_LE"))
    private LegalEntity legalEntity;

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
