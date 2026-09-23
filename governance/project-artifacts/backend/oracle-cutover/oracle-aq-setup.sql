-- ============================================================================
-- oracle-aq-setup.sql — accounting event queue (Oracle, schema LOAN_SYS)         [file 1 of 3]
-- ============================================================================
-- Prepared 2026-09-23 against LOAN_SYS@FREEPDB1 (Oracle AI Database 26ai Free 23.26.2.0.0,
-- compatible = 23.6.0, DBMS_AQADM.CREATE_TRANSACTIONAL_EVENT_QUEUE present — verified read-only).
-- Design: governance/project-artifacts/oracle-aq-fin-integration-plan.md §2.1, §2.3, §3.2, §3.4, §3.5.
-- Order on production: 1 this file → 2 oracle-event-emit-package.sql → 3 oracle-event-emit-triggers.sql
--                      → (consumer running, shadow verified) → legacy-accounting-stop.sql.
--
-- WHAT THIS CREATES (all additive, all reversible — see ROLLBACK at the end)
--   ACCOUNTING_EVENT_SEQ   sequence — the per-emission suffix of eventReference (uniqueness without
--                          adding a revision column to any legacy table)
--   ACCOUNTING_EVENT_Q     Transactional Event Queue, JSON payload, single consumer, single shard
--                          (= global FIFO), max_retries 5, exception queue automatic.
--                          retry_delay is not a TxEventQ setting — the consumer owns back-off.
--   ACCOUNTING_EVENT_LOG   a plain table: every emission's reference, type, payload and outcome.
--                          The queue is the transport; this is the audit and the replay source.
--                          (governance plan §3.3 wants a consumer-side inbox too — that lives in
--                          PostgreSQL, not here.)
--
-- WHY TxEventQ AND NOT CLASSIC AQ
--   Oracle's current guidance for new applications on 21c+/23ai is TxEventQ (plan §2.1). The
--   enqueue call (DBMS_AQ.ENQUEUE) and the JMS consumer are identical for both, so the only
--   thing that would change on an edition without TxEventQ is this file — see the FALLBACK block.
--
-- WHY ONE CONSUMER, FIFO
--   Per-document ordering is a correctness property (an update = reversal + new entry — plan §3.5).
--   A single dequeuing thread on a FIFO queue guarantees it without message groups. 2026 volume
--   (≈ 4,800 journal-producing rows in 9 months) is far below what one thread handles.
--
-- HOW TO RUN     sqlplus LOAN_SYS/...@//host:1521/FREEPDB1 @oracle-aq-setup.sql   (as LOAN_SYS; it holds DBA)
-- IDEMPOTENT     every step checks for the object first and skips it if present.
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
WHENEVER SQLERROR EXIT FAILURE

-- ----------------------------------------------------------------------------
-- 0. Direct EXECUTE on DBMS_AQ — a PREREQUISITE, not a nicety
-- ----------------------------------------------------------------------------
-- LOAN_SYS holds DBA, but only as a ROLE, and it has no direct object grants at all
-- (user_tab_privs on DBMS_AQ/DBMS_AQADM: no rows). Roles are active in an anonymous block and
-- disabled inside a definer-rights stored unit, so DBMS_AQADM calls in this file succeed while
-- PKG_ACCOUNTING_EVENT's body fails to compile with
--     PLS-00201: identifier 'DBMS_AQ' must be declared
-- Discovered on the first real run, 2026-09-23. A direct grant is the documented AQ requirement.
-- Granted here via DBA's GRANT ANY OBJECT PRIVILEGE, so no SYS login is needed; on a hardened
-- production instance a DBA runs the same statement instead:
--     GRANT EXECUTE ON SYS.DBMS_AQ TO LOAN_SYS;
-- Reverse with: REVOKE EXECUTE ON SYS.DBMS_AQ FROM LOAN_SYS;
DECLARE n NUMBER;
BEGIN
  SELECT COUNT(*) INTO n FROM user_tab_privs
   WHERE table_name = 'DBMS_AQ' AND privilege = 'EXECUTE';
  IF n = 0 THEN
    EXECUTE IMMEDIATE 'GRANT EXECUTE ON SYS.DBMS_AQ TO LOAN_SYS';
    DBMS_OUTPUT.PUT_LINE('granted EXECUTE ON SYS.DBMS_AQ to LOAN_SYS');
  ELSE
    DBMS_OUTPUT.PUT_LINE('EXECUTE ON SYS.DBMS_AQ already granted directly — skipped');
  END IF;
END;
/

-- ----------------------------------------------------------------------------
-- 1. Sequence — eventReference suffix
-- ----------------------------------------------------------------------------
DECLARE n NUMBER;
BEGIN
  SELECT COUNT(*) INTO n FROM user_sequences WHERE sequence_name = 'ACCOUNTING_EVENT_SEQ';
  IF n = 0 THEN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE ACCOUNTING_EVENT_SEQ START WITH 1 INCREMENT BY 1 CACHE 100 NOCYCLE';
    DBMS_OUTPUT.PUT_LINE('created ACCOUNTING_EVENT_SEQ');
  ELSE
    DBMS_OUTPUT.PUT_LINE('ACCOUNTING_EVENT_SEQ exists — skipped');
  END IF;
END;
/

-- ----------------------------------------------------------------------------
-- 2. Emission log — audit + replay source (plain table, no FK into legacy tables)
-- ----------------------------------------------------------------------------
DECLARE n NUMBER;
BEGIN
  SELECT COUNT(*) INTO n FROM user_tables WHERE table_name = 'ACCOUNTING_EVENT_LOG';
  IF n = 0 THEN
    EXECUTE IMMEDIATE q'[
      CREATE TABLE ACCOUNTING_EVENT_LOG (
        EVENT_LOG_PK      NUMBER          GENERATED ALWAYS AS IDENTITY,
        EVENT_REFERENCE   VARCHAR2(100)   NOT NULL,
        EVENT_TYPE_CODE   VARCHAR2(50)    NOT NULL,
        SOURCE_TABLE      VARCHAR2(30)    NOT NULL,
        SOURCE_PK         NUMBER          NOT NULL,
        SOURCE_ACTION     VARCHAR2(10)    NOT NULL,   -- INSERT | UPDATE | DELETE | REPLAY
        DOC_DATE          DATE            NOT NULL,
        BASE_AMOUNT       NUMBER(18,4)    NOT NULL,
        PAYLOAD           JSON            NOT NULL,
        ENQUEUED_FL       NUMBER(1)       DEFAULT 1 NOT NULL,   -- 0 = emit failed, payload kept for replay
        ENQUEUE_ERROR     VARCHAR2(4000),
        CREATED_DATE      TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
        CREATED_BY        VARCHAR2(100)   DEFAULT SYS_CONTEXT('USERENV','SESSION_USER'),
        CONSTRAINT PK_ACCOUNTING_EVENT_LOG PRIMARY KEY (EVENT_LOG_PK),
        CONSTRAINT UQ_ACCOUNTING_EVENT_LOG_REF UNIQUE (EVENT_REFERENCE),
        CONSTRAINT CHK_ACCOUNTING_EVENT_LOG_ACTION CHECK (SOURCE_ACTION IN ('INSERT','UPDATE','DELETE','REPLAY'))
      )]';
    EXECUTE IMMEDIATE 'CREATE INDEX IX_ACCOUNTING_EVENT_LOG_SRC ON ACCOUNTING_EVENT_LOG (SOURCE_TABLE, SOURCE_PK)';
    EXECUTE IMMEDIATE 'CREATE INDEX IX_ACCOUNTING_EVENT_LOG_DATE ON ACCOUNTING_EVENT_LOG (DOC_DATE)';
    EXECUTE IMMEDIATE q'[COMMENT ON TABLE ACCOUNTING_EVENT_LOG IS 'Every accounting event emitted to ACCOUNTING_EVENT_Q — audit and replay source. Written by PKG_ACCOUNTING_EVENT only. Additive object of the FIN cutover (2026-09).']';
    DBMS_OUTPUT.PUT_LINE('created ACCOUNTING_EVENT_LOG');
  ELSE
    DBMS_OUTPUT.PUT_LINE('ACCOUNTING_EVENT_LOG exists — skipped');
  END IF;
END;
/

-- ----------------------------------------------------------------------------
-- 3. The queue — TxEventQ, JSON payload
-- ----------------------------------------------------------------------------
DECLARE n NUMBER;
BEGIN
  SELECT COUNT(*) INTO n FROM user_queues WHERE name = 'ACCOUNTING_EVENT_Q';
  IF n = 0 THEN
    -- max_retries is an argument of CREATE, not a later ALTER. Corrected 2026-09-23 after the
    -- first real run: DBMS_AQADM.ALTER_QUEUE raises ORA-24218 ("feature ALTER_QUEUE not supported
    -- for transactional event queues"), and because CREATE had already committed, that left the
    -- queue existing but never started (enqueue NO / dequeue NO). Do not reintroduce ALTER_QUEUE.
    DBMS_AQADM.CREATE_TRANSACTIONAL_EVENT_QUEUE(
      queue_name         => 'ACCOUNTING_EVENT_Q',
      queue_payload_type => 'JSON',
      multiple_consumers => FALSE,          -- one subscriber: the event consumer (plan §3.1)
      max_retries        => 5,              -- then the message goes to the exception queue (plan §2.3, §3.4)
      comment            => 'Canonical accounting events LOAN_SYS -> FIN. Business facts only; never an account code (plan §3.2).');

    -- retry_delay: NOT SETTABLE on a TxEventQ. GET_QUEUE_PARAMETER accepts only SHARD_NUM,
    -- KEY_BASED_ENQUEUE and STICKY_DEQUEUE; RETRY_DELAY / RETRY_INTERVAL / MAX_RETRIES all raise
    -- ORA-00904 "Unsupported param". The 60 s spacing the plan asks for is therefore the
    -- CONSUMER's responsibility (back-off between redelivery attempts), not the queue's.

    -- FIFO. A TxEventQ is sharded — this instance defaults to SHARD_NUM = 5 — and order is
    -- guaranteed only WITHIN a shard. The plan's per-document ordering (an update is a reversal
    -- plus a new entry, §3.5) is a correctness property, so collapse to a single shard: with one
    -- shard and one consumer the queue is globally FIFO, which is what §2.1 assumed all along.
    -- Volume (~4,800 journal-producing rows in 9 months) is nowhere near needing 5 shards.
    DBMS_AQADM.SET_QUEUE_PARAMETER('ACCOUNTING_EVENT_Q', 'SHARD_NUM', 1);

    DBMS_AQADM.START_QUEUE(queue_name => 'ACCOUNTING_EVENT_Q', enqueue => TRUE, dequeue => TRUE);
    DBMS_OUTPUT.PUT_LINE('created + started ACCOUNTING_EVENT_Q (TxEventQ, JSON, max_retries 5, 1 shard)');
  ELSE
    -- The queue exists. That is NOT the same as the queue being usable: CREATE commits on its
    -- own, so a failure anywhere later in this block (as happened on the first real run) leaves a
    -- queue that exists, has the wrong shard count and is not started. "Skip if present" would
    -- then never repair it. So converge the settings instead of assuming them.
    DBMS_AQADM.SET_QUEUE_PARAMETER('ACCOUNTING_EVENT_Q', 'SHARD_NUM', 1);
    DBMS_AQADM.START_QUEUE(queue_name => 'ACCOUNTING_EVENT_Q', enqueue => TRUE, dequeue => TRUE);
    DBMS_OUTPUT.PUT_LINE('ACCOUNTING_EVENT_Q existed — shard count and start state reasserted');
  END IF;
END;
/

-- FALLBACK — only if CREATE_TRANSACTIONAL_EVENT_QUEUE is unavailable on the target edition.
-- Same queue name, same enqueue call, same JMS consumer; sort_list is immutable after creation
-- (plan §2.3), so it is decided here: ENQ_TIME = FIFO.
--   BEGIN
--     DBMS_AQADM.CREATE_QUEUE_TABLE(queue_table => 'ACCOUNTING_EVENT_QT', queue_payload_type => 'JSON',
--                                   sort_list => 'ENQ_TIME', multiple_consumers => FALSE,
--                                   comment => 'Canonical accounting events LOAN_SYS -> FIN');
--     DBMS_AQADM.CREATE_QUEUE(queue_name => 'ACCOUNTING_EVENT_Q', queue_table => 'ACCOUNTING_EVENT_QT',
--                             max_retries => 5, retry_delay => 60);
--     DBMS_AQADM.START_QUEUE(queue_name => 'ACCOUNTING_EVENT_Q');
--   END;
--   /

-- ----------------------------------------------------------------------------
-- 4. Verification (read-only)
-- ----------------------------------------------------------------------------
SELECT name, queue_type, enqueue_enabled, dequeue_enabled, max_retries, retry_delay, queue_table
  FROM user_queues WHERE name LIKE 'ACCOUNTING_EVENT_Q%' OR name LIKE 'AQ$_ACCOUNTING%' ORDER BY name;
-- expected: ACCOUNTING_EVENT_Q NORMAL_QUEUE enq=YES deq=YES max_retries=5 retry_delay=0
--           (retry_delay is always 0 on a TxEventQ — see section 3; it is not a defect)
DECLARE v NUMBER; BEGIN
  DBMS_AQADM.GET_QUEUE_PARAMETER('ACCOUNTING_EVENT_Q', 'SHARD_NUM', v);
  DBMS_OUTPUT.PUT_LINE('SHARD_NUM = '||v||'   (expected 1 — anything else breaks global FIFO)');
END;
/
SELECT sequence_name FROM user_sequences WHERE sequence_name = 'ACCOUNTING_EVENT_SEQ';
SELECT COUNT(*) AS log_rows FROM ACCOUNTING_EVENT_LOG;   -- expected: 0 on first run

-- ============================================================================
-- ROLLBACK COMPANION — reverse order; the log table is kept unless you really mean it
-- ============================================================================
-- BEGIN DBMS_AQADM.STOP_QUEUE(queue_name => 'ACCOUNTING_EVENT_Q'); DBMS_AQADM.DROP_TRANSACTIONAL_EVENT_QUEUE(queue_name => 'ACCOUNTING_EVENT_Q'); END;
-- /
-- -- classic fallback instead: BEGIN DBMS_AQADM.STOP_QUEUE('ACCOUNTING_EVENT_Q'); DBMS_AQADM.DROP_QUEUE('ACCOUNTING_EVENT_Q'); DBMS_AQADM.DROP_QUEUE_TABLE('ACCOUNTING_EVENT_QT'); END;
-- DROP SEQUENCE ACCOUNTING_EVENT_SEQ;
-- -- DROP TABLE ACCOUNTING_EVENT_LOG PURGE;   -- deliberately commented twice: it is the audit trail
