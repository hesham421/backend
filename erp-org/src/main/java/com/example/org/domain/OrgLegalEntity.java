package com.example.org.domain;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORG_LEGAL_ENTITY",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ORG_LE_CODE", columnNames = {"LEGAL_ENTITY_CODE"})
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

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "LEGAL_ENTITY_CODE", length = 20, nullable = false)
    private String legalEntityCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // LOV-ORG-001: HEAD_OFFICE, BRANCH_OFFICE, SUBSIDIARY, REPRESENTATIVE_OFFICE
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

    @OneToMany(mappedBy = "legalEntity", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrgBranch> branches = new ArrayList<>();

    @OneToMany(mappedBy = "legalEntity", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrgRegion> regions = new ArrayList<>();

    @OneToMany(mappedBy = "legalEntity", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrgProfitCenter> profitCenters = new ArrayList<>();

    // RULE-ORG-001 guard count
    @Formula("(SELECT COUNT(*) FROM ORG_BRANCH b WHERE b.LEGAL_ENTITY_FK = LEGAL_ENTITY_PK AND b.IS_ACTIVE_FL = 1)")
    private Integer activeBranchCount;

    // RULE-ORG-002 guard count
    @Formula("(SELECT COUNT(*) FROM ORG_PROFIT_CENTER pc WHERE pc.LEGAL_ENTITY_FK = LEGAL_ENTITY_PK AND pc.IS_ACTIVE_FL = 1)")
    private Integer activeProfitCenterCount;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) isActiveFl = Boolean.TRUE;
    }

    public void activate() { this.isActiveFl = Boolean.TRUE; }
    public void deactivate() { this.isActiveFl = Boolean.FALSE; }
}
