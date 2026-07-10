package com.example.security.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * PASSWORD_RESET_TOKEN (ENTITY-SEC-011) — single-use password reset token. Infrastructure
 * table, no soft-delete (usedFl flips true on consumption per RULE-SEC-033); modeled on
 * {@link RefreshToken}'s plain-audit-field style (not AuditableEntity), consistent with
 * db-script-SEC-gaps.md BLOCK 3 giving this table only CREATED_AT/EXPIRES_AT, no
 * CREATED_BY/UPDATED_AT/UPDATED_BY.
 */
@Entity
@Table(name = "PASSWORD_RESET_TOKEN",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_PASSWORD_RESET_TOKEN_TOKEN", columnNames = {"TOKEN"})
    },
    indexes = {
        @Index(name = "IDX_PASSWORD_RESET_TOKEN_USER", columnList = "USER_ID_FK"),
        @Index(name = "IDX_PASSWORD_RESET_TOKEN_EXPIRES", columnList = "EXPIRES_AT")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "password_reset_token_seq")
    @SequenceGenerator(name = "password_reset_token_seq", sequenceName = "PASSWORD_RESET_TOKEN_SEQ", allocationSize = 1)
    @Column(name = "TOKEN_PK")
    private Long tokenPk;

    @Column(name = "TOKEN", nullable = false, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID_FK", referencedColumnName = "USERS_PK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_PASSWORD_RESET_TOKEN_USER"))
    private UserAccount user;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name = "USED_FL", nullable = false)
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean usedFl = Boolean.FALSE;

    public boolean isUsed() {
        return Boolean.TRUE.equals(usedFl);
    }

    public void setUsed(boolean used) {
        this.usedFl = used;
    }
}
