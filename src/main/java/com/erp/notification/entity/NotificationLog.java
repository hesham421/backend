package com.erp.notification.entity;

import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * JPA entity for NOTIF_LOG (ENTITY-NOTIF-001, DBF-0001..0016). SHARED (owner) — consumer:
 * AuditService (SOFT-READ, NOT-YET-ASSIGNED). No Business Code — engine-managed, append-only
 * system log. Create is system-only (at send time); no manual Update/Delete, only the
 * status/retryCount transitions below. No domain/ package — per CORE.md (3-entity module
 * scale, embedded entity methods).
 *
 * <p>{@code recipientId} is a plain scalar FK to Security's {@code USERS.USERS_PK}, not a JPA
 * association — erp-notification has no Maven dependency on erp-security, same
 * cross-module scalar-FK pattern already used elsewhere in this codebase (e.g.
 * {@code SecUserProfile}/{@code SecRoleBranch} → ORG's branch id). The FK is still enforced
 * live at the DB level (FK_NOTIF_LOG_USERS — see DATAOM migration), a documented Security
 * EXCEPTION, not an XM-ID.
 */
@Entity
@Table(name = "NOTIF_LOG")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class NotificationLog extends AuditableEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CHANNEL_DISABLED = "CHANNEL_DISABLED";

    /** RULE-NOTIF-004 — retry ceiling; enforced by the dispatch orchestration, not this entity. */
    public static final short MAX_RETRY_COUNT = 5;

    /** Sweep-retry ceiling for {@code FailedNotificationSweepScheduler} — see V15 migration comment. */
    public static final short MAX_SWEEP_RETRY_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notif_log_seq")
    @SequenceGenerator(name = "notif_log_seq", sequenceName = "SEQ_NOTIF_LOG", allocationSize = 1)
    @Column(name = "NOTIFICATION_LOG_PK")
    private Long id;

    // FIELD-0002 — FK → Security USERS.USERS_PK. PERMANENT EXCEPTION column name (not usersFk).
    @NotNull(message = "{validation.required}")
    @Column(name = "RECIPIENT_ID", nullable = false)
    private Long recipientId;

    // FIELD-0003 — LOV-NOTIF-001, the channel used for THIS row (one row per requested channel — RULE-NOTIF-003)
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "NOTIFICATION_TYPE_ID", length = 20, nullable = false)
    private String notificationTypeId;

    // FIELD-0004 — natural-key logical reference to NOTIF_TEMPLATE.TEMPLATE_CODE, NO physical FK
    // (graceful fallback per RULE-NOTIF-006 — a hard FK would contradict the fallback design)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "TEMPLATE_CODE", length = 50, nullable = false)
    private String templateCode;

    // FIELD-0005 — primarily Email channel
    @Size(max = 500, message = "{validation.size}")
    @Column(name = "SUBJECT", length = 500)
    private String subject;

    // FIELD-0006 — was VARCHAR(1000); widened to unbounded TEXT (same @Lob +
    // JdbcTypeCode(SqlTypes.LONGVARCHAR) + columnDefinition="text" pattern as
    // NotificationTemplate.templateBodyAr/En) now that the EMAIL channel sends real HTML — the
    // old 1000-char cap was clipping rendered bodies mid-tag.
    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "BODY_PREVIEW", columnDefinition = "text")
    private String bodyPreview;

    // FIELD-0007 — LOV-NOTIF-002, Status Lifecycle (4 states)
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "NOTIFICATION_STATUS_ID", length = 20, nullable = false)
    @Builder.Default
    private String notificationStatusId = STATUS_PENDING;

    // FIELD-0008 — default 0, ceiling 5 (RULE-NOTIF-004). SMALLINT → Short, NOT the _FL Boolean
    // convention (DRV-NOTIF-001) — this is a count, not a flag.
    @NotNull(message = "{validation.required}")
    @Column(name = "RETRY_COUNT", nullable = false)
    @Builder.Default
    private Short retryCount = 0;

    // FIELD-0009 — null until sent
    @Column(name = "SENT_AT")
    private Instant sentAt;

    // FIELD-0010 — publishing module code
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "MODULE_CODE", length = 20, nullable = false)
    private String moduleCode;

    // FIELD-0011 — polymorphic, NO physical FK (same pattern as FILE_DOCUMENT.OWNER_ID)
    @Column(name = "REFERENCE_ID")
    private Long referenceId;

    // FIELD-0012 — free text, NOT a governed LOV
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "REFERENCE_TYPE", length = 50)
    private String referenceType;

    // FIELD (post-implementation-audit remediation, Item 2) — separate from retryCount; see
    // V15__notif_log_sweep_retry_count.sql for why this is a distinct counter.
    @NotNull(message = "{validation.required}")
    @Column(name = "SWEEP_RETRY_COUNT", nullable = false)
    @Builder.Default
    private Short sweepRetryCount = 0;

    /** RULE-NOTIF-003/RULE-13 — one-way terminal transition, send succeeded. */
    public void markSent() {
        this.notificationStatusId = STATUS_SENT;
        this.sentAt = Instant.now();
    }

    /** RULE-NOTIF-004/RULE-13 — one-way terminal transition, retries exhausted. */
    public void markFailed() {
        this.notificationStatusId = STATUS_FAILED;
    }

    /** RULE-NOTIF-005/RULE-13 — one-way terminal transition, channel disabled at send time. */
    public void markChannelDisabled() {
        this.notificationStatusId = STATUS_CHANNEL_DISABLED;
    }

    /**
     * RULE-NOTIF-004 — increments the delivery retry counter. Called by the dispatch
     * orchestration on each failed send attempt; the ceiling of {@link #MAX_RETRY_COUNT} is
     * enforced by the caller (which then calls {@link #markFailed()}), not here.
     */
    public void incrementRetry() {
        this.retryCount = (short) (this.retryCount + 1);
    }

    /**
     * Called by {@code FailedNotificationSweepScheduler} on each failed later re-attempt of an
     * already-terminal {@link #STATUS_FAILED} row. Does not change {@link #notificationStatusId}
     * — the row stays {@code FAILED} (queryable, unchanged terminal status) until either a sweep
     * attempt succeeds ({@link #markSent()}) or {@link #MAX_SWEEP_RETRY_COUNT} is reached, at
     * which point the scheduler stops selecting it.
     */
    public void incrementSweepRetry() {
        this.sweepRetryCount = (short) (this.sweepRetryCount + 1);
    }
}
