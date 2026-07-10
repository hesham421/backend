package com.example.security.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "REFRESH_TOKENS")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshToken {

    /**
     * PK constraint name: REFRESH_TOKENS_PK (matches the column name below).
     * Naming the constraint itself isn't expressible via a JPA annotation on
     * @Id (unlike @ForeignKey for FKs) — Hibernate's naming-strategy hooks
     * only cover FOREIGN_KEY/UNIQUE_KEY/INDEX, never PRIMARY_KEY — so the
     * constraint name is enforced in the live DB by
     * 001_rename_pk_fk_to_standard.sql instead.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REFRESH_TOKENS_PK")
    private Long id;

    @Column(name="JTI", nullable=false, unique=true, length=64)
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="USER_ID_FK", referencedColumnName = "USERS_PK", nullable=false,
        foreignKey = @ForeignKey(name = "FK_RT_USER"))
    private UserAccount user;

    @CreationTimestamp
    @Column(name="CREATED_AT", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="EXPIRES_AT", nullable=false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name="REVOKED", nullable=false)
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean revoked = Boolean.FALSE;

    public boolean isRevoked() {
        return Boolean.TRUE.equals(revoked);
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
