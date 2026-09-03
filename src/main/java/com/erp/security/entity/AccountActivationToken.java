package com.erp.security.entity;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * ENTITY-SEC-007 — AccountActivationToken (self-service activation artifact). Source:
 * db-script-SEC.md DBS-SEC-001, DATA-DOM-TOKENS.md. Internal entity — no nameAr/nameEn, no
 * screen. The TOKEN value is stored hashed only (RULE-SEC-004 / DRV-005) — the service hashes
 * before persisting. Single active, single-use (RULE-SEC-008). Persistence-only; consumability
 * decisions live in AccountActivationTokenDomain. Extends AuditableEntity per CORE.md (db-script
 * defines the audit columns).
 */
@Entity
@Table(name = "SEC_ACCOUNT_ACTIVATION_TOKEN",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_SEC_ACT_TOKEN_TOKEN", columnNames = {"TOKEN"})
    },
    indexes = {
        @Index(name = "IDX_SEC_ACT_TOKEN_USER_FK", columnList = "USER_ACCOUNT_FK")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class AccountActivationToken extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_activation_token_seq")
    @SequenceGenerator(name = "account_activation_token_seq", sequenceName = "SEQ_SEC_ACCOUNT_ACTIVATION_TOKEN", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Size(max = 255, message = "{validation.size}")
    @Column(name = "TOKEN", length = 255, nullable = false)
    private String token;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "USED_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean used = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ACCOUNT_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_SEC_ACT_TOKEN_USER"))
    private UserAccount userAccount;

    @PrePersist
    protected void onCreate() {
        if (used == null) {
            used = Boolean.FALSE;
        }
    }

    public void markUsed() {
        this.used = Boolean.TRUE;
    }
}
