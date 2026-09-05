package com.erp.mdm.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * ENTITY-MDM-002 — LookupValue (detail reference-data value under a type). Source:
 * db-script-MDM.md DBS-MDM-001, DATA-DOM.md. valueCode is the immutable natural key
 * (RULE-MDM-004), unique within its parent type. Persistence-only; decisions live in
 * LookupValueDomain. Never exists without a master LookupType (lookupType FK NOT NULL).
 */
@Entity
@Table(name = "MDM_LOOKUP_VALUE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_MDM_LOOKUP_VALUE_TYPE_CODE", columnNames = {"LOOKUP_TYPE_FK", "VALUE_CODE"})
    },
    indexes = {
        @Index(name = "IDX_MDM_LOOKUP_VALUE_TYPE_FK", columnList = "LOOKUP_TYPE_FK")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class LookupValue extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lookup_value_seq")
    @SequenceGenerator(name = "lookup_value_seq", sequenceName = "SEQ_MDM_LOOKUP_VALUE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOOKUP_TYPE_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_MDM_LOOKUP_VALUE_TYPE"))
    private LookupType lookupType;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "VALUE_CODE", length = 50, nullable = false)
    private String valueCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    @Column(name = "SORT_ORDER")
    private Short sortOrder;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActive = Boolean.TRUE;

    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
        if (valueCode != null) {
            valueCode = valueCode.trim().toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (valueCode != null) {
            valueCode = valueCode.trim().toUpperCase();
        }
    }

    public void activate() {
        this.isActive = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }
}
