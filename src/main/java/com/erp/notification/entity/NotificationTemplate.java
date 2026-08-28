package com.erp.notification.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity for NOTIF_TEMPLATE. {@code fileFk} stays nullable/unused until File Service's
 * deferred migration (XM-NOTIF-001). {@code templateCode} is a manually-assigned,
 * immutable-after-creation natural code, enforced at the Service layer.
 */
@Entity
@Table(name = "NOTIF_TEMPLATE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_NOTIF_TEMPLATE_CODE", columnNames = {"TEMPLATE_CODE"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class NotificationTemplate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notif_template_seq")
    @SequenceGenerator(name = "notif_template_seq", sequenceName = "SEQ_NOTIF_TEMPLATE", allocationSize = 1)
    @Column(name = "NOTIFICATION_TEMPLATE_PK")
    private Long id;

    // FIELD-0018 — manually-assigned natural code, unique, immutable post-create (RULE-NOTIF-007)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "TEMPLATE_CODE", length = 50, nullable = false)
    private String templateCode;

    // FIELD-0019
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "TEMPLATE_NAME_AR", length = 200, nullable = false)
    private String templateNameAr;

    // FIELD-0020
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "TEMPLATE_NAME_EN", length = 200, nullable = false)
    private String templateNameEn;

    // FIELD-0021 — LOV-NOTIF-001 (NOTIFICATION_CHANNEL), target channel for this template
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "CHANNEL_TYPE_ID", length = 20, nullable = false)
    private String channelTypeId;

    // FIELD-0022 — owning module, free text
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "MODULE_CODE", length = 20, nullable = false)
    private String moduleCode;

    // FIELD-0023 — Phase-1 inline storage, supports {{placeholder}} syntax. TEXT → String + @Lob.
    // @JdbcTypeCode(SqlTypes.LONGVARCHAR) forces Postgres TEXT binding — without it, Hibernate's
    // default @Lob+String mapping on the PostgreSQL dialect is the Large Object API (oid column
    // type), which doesn't match this TEXT column and makes ddl-auto=update try (and fail) an
    // ALTER COLUMN ... SET DATA TYPE oid on every startup. columnDefinition = "text" is required
    // too — without it, Hibernate infers a bounded varchar(32600) DDL instead of unbounded TEXT.
    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @NotBlank(message = "{validation.required}")
    @Column(name = "TEMPLATE_BODY_AR", nullable = false, columnDefinition = "text")
    private String templateBodyAr;

    // FIELD-0024 — Phase-1 inline storage. TEXT → String + @Lob. See TEMPLATE_BODY_AR above for
    // why @JdbcTypeCode(SqlTypes.LONGVARCHAR) + columnDefinition = "text" are required alongside @Lob.
    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @NotBlank(message = "{validation.required}")
    @Column(name = "TEMPLATE_BODY_EN", nullable = false, columnDefinition = "text")
    private String templateBodyEn;

    // FIELD-0025 — DEFERRED, unused Phase 1 — XM-NOTIF-001 (see INT-C/INT-R). No JPA association:
    // FileDocument lives in a separate Maven module (erp-file) not yet a dependency of
    // erp-notification, and the FK itself is deferred until File Service's migration path fires.
    @Column(name = "FILE_FK")
    private Long fileFk;

    // FIELD-0026 — default true
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }

    /**
     * Returns the body in the recipient's resolved language. If no active template is found at all,
     * the caller (fan-out processor) substitutes a platform-default template — that fallback does
     * not live here.
     */
    public String resolveBody(String languageCode) {
        return "AR".equalsIgnoreCase(languageCode) ? templateBodyAr : templateBodyEn;
    }
}
