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
 * ENTITY-SEC-005 — RefreshToken (JWT refresh session artifact). Source: db-script-SEC.md
 * DBS-SEC-001, DATA-DOM-TOKENS.md. Internal entity — no nameAr/nameEn, no screen. The TOKEN
 * value is stored hashed only (RULE-SEC-004 / DRV-005) — the service hashes before persisting.
 * Rotated on refresh (RULE-SEC-006). Persistence-only; usability/rotation decisions live in
 * RefreshTokenDomain. Extends AuditableEntity per CORE.md (db-script defines the audit columns).
 */
@Entity
@Table(name = "SEC_REFRESH_TOKEN",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_SEC_REFRESH_TOKEN_TOKEN", columnNames = {"TOKEN"})
    },
    indexes = {
        @Index(name = "IDX_SEC_REFRESH_TOKEN_USER_FK", columnList = "USER_ACCOUNT_FK")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class RefreshToken extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_seq")
    @SequenceGenerator(name = "refresh_token_seq", sequenceName = "SEQ_SEC_REFRESH_TOKEN", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Size(max = 255, message = "{validation.size}")
    @Column(name = "TOKEN", length = 255, nullable = false)
    private String token;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "REVOKED_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean revoked = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ACCOUNT_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_SEC_REFRESH_TOKEN_USER"))
    private UserAccount userAccount;

    @PrePersist
    protected void onCreate() {
        if (revoked == null) {
            revoked = Boolean.FALSE;
        }
    }

    public void revoke() {
        this.revoked = Boolean.TRUE;
    }
}
