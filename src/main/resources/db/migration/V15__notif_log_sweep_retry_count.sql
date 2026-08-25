-- ============================================================================
-- Post-implementation-audit remediation, Item 2 — durable retry for a
-- sustained (all-5-in-process-attempts-exhausted) EMAIL/SMTP outage.
--
-- RULE-NOTIF-004's existing retry (NotificationDispatchService, retry_count,
-- up to MAX_RETRY_COUNT=5, in-process backoff) already handles a short-lived
-- transient failure. It has no fallback once exhausted: a NOTIF_LOG row just
-- sits at notification_status_id='FAILED' forever, with no later re-attempt,
-- if e.g. Gmail SMTP is down for longer than ~16 seconds (the total backoff
-- window). sweep_retry_count is a SEPARATE counter for a periodic @Scheduled
-- sweep (FailedNotificationSweepScheduler) that re-attempts FAILED rows much
-- later (minutes, not seconds) — distinct from retry_count so the fast
-- in-process retry's own ceiling isn't disturbed by this slower, independent
-- durable-retry layer.
-- ============================================================================

BEGIN;

ALTER TABLE notif_log ADD COLUMN sweep_retry_count SMALLINT NOT NULL DEFAULT 0;

COMMIT;
