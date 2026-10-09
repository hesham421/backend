-- ============================================================================
-- 02-package-19c.sql — PKG_ACCOUNTING_EVENT, Oracle 19c port        [19c file 2 of 4]
-- ============================================================================
-- 19c PORT (2026-09-25): logic IDENTICAL to oracle-event-emit-package.sql (D-16, D-19, D-20, D-21).
-- Only the JSON carriers change: SQL type JSON / to_json() (21c+) → CLOB / to_clob(); the enqueue
-- payload is ACCOUNTING_EVENT_MSG_T(<clob>) (19c/01-aq-setup-19c.sql). JSON_OBJECT_T is 12.2+.
-- ---------------------------------------------------------------------------- original header:
-- Prepared 2026-09-23. Requires oracle-aq-setup.sql (ACCOUNTING_EVENT_Q, ACCOUNTING_EVENT_SEQ,
-- ACCOUNTING_EVENT_LOG). Consumed by oracle-event-emit-triggers.sql (file 3).
-- Contract: FIN v2 API-FIN-020 (governance/shared/backend/modules/FIN/packages/v2/backend-execution/
-- SVC-API/SVC-API-INT.md) — fields{} keyed by FIN_EVENT_BUSINESS_FIELD codes; the plan's §3.2.
--
-- THE BOUNDARY, ENFORCED HERE
--   This package never reads ACCOUNTS_CHART, DAILY_RESTRICTIONS_*, or any template row, and no
--   account code, account id, SCREEN_FK, RESTRICTIONS_TYPE_FK or debit/credit designation exists
--   in anything it emits. It sends business facts; FIN decides the accounting.
--
-- ONE PROCEDURE, ONE JOB
--   emit(...) builds the canonical JSON, inserts an ACCOUNTING_EVENT_LOG row, enqueues on
--   ACCOUNTING_EVENT_Q — all inside the caller's transaction (a rolled-back business save takes
--   its event with it; the queue is the outbox). It NEVER raises: any failure is logged with
--   ENQUEUED_FL = 0 and swallowed, because a user who cannot save a payment is worse than an
--   event that has to be replayed (plan §5 Phase 3). Replay reads the log.
--
-- CONVENTIONS EMITTED
--   eventReference   LEGACY:<TABLE>:<PK>:<ACCOUNTING_EVENT_SEQ>   — unique per emission, so an
--                    UPDATE (reversal + re-post) or a DELETE (reversal) never reuses a reference
--   eventTypeCode    the canonical type; when the amount's sign is the opposite of the branch's
--                    normal sign the type gets the suffix _REVERSED and the amount is sent as ABS
--                    (legacy flipped debit/credit on sign — findings F-10 / D-14: FIN needs the
--                    mirrored _REVERSED rules, seeded by a later delta)
--   docDate          the business date column of the row (LOAN_PAYMENT_DATE, COMPLAINT_DT_DATE,
--                    INV_IMPORT_DATE, EXPENSE_DATE, TRANSFER_SAFE_DATE, CONTRACT_DATE,
--                    QUITTANCE_DATE); legacy stamped SYSDATE — findings D-15
--   fields.PAYMENT_METHOD    PAYMENT_TYPE_FK 1 → CHEQUE, anything else (0 or NULL) → CASH,
--                    exactly the legacy CASE (24 stays 24 unless PAYMENT_TYPE_FK = 1)
--   fields.ORGANISATION_CODE / BRANCH_CODE   the Oracle PKs as strings (= FIN_DIMENSION_VALUE.code)
--   fields.EXPENSE_TYPE_CODE EXPENSE_TYPE_ID as a string (= FIN_ACCOUNT_MAPPING.business_value)
--   descriptions     the legacy note prefixes, verbatim, so the two ledgers read alike
--
-- ZERO AMOUNTS are not enqueued (FIN rejects amount <= 0 by CHECK); they are logged with
--   ENQUEUED_FL = 0, ENQUEUE_ERROR = 'ZERO_AMOUNT_SKIPPED' so nothing is silently dropped.
--
-- SESSION SWITCH   PKG_ACCOUNTING_EVENT.set_enabled(FALSE) silences emission for the current
--   session only (bulk maintenance, replays that emit through another path). Default TRUE.
--
-- HOW TO RUN   sqlplus LOAN_SYS/...@//host:1521/FREEPDB1 @oracle-event-emit-package.sql
-- ROLLBACK     DROP PACKAGE PKG_ACCOUNTING_EVENT;   (file 3's triggers must be dropped/disabled first)
-- ============================================================================

SET ECHO ON
SET SERVEROUTPUT ON SIZE UNLIMITED
WHENEVER SQLERROR EXIT FAILURE

CREATE OR REPLACE PACKAGE PKG_ACCOUNTING_EVENT AUTHID DEFINER AS

  c_queue        CONSTANT VARCHAR2(30) := 'ACCOUNTING_EVENT_Q';
  -- Named on EVERY message (D-20): on 23.26.2 a TxEventQ exception queue created by
  -- CREATE_EQ_EXCEPTION_QUEUE receives only messages whose enqueue set exception_queue. Without it
  -- a message past max_retries stays RETRYEXPIRED in the queue table, dequeueable from nowhere.
  c_exception_queue CONSTANT VARCHAR2(61) := 'ACCOUNTING_EVENT_Q_EXCPT';

  -- EXPENSE_TYPE_DT rows that TRIGGER_COMPLAINT_DT_EXP_DT mirrors from COMPLAINT_DT
  -- (COMPLAINT_DT_FK IS NOT NULL) are NOT skipped. Measured 2026 (findings OQ-14, closed): all 112
  -- mirrors come from COMPLAINT_DT_FL = 50 'ايراد شكوي', which the legacy COMPLAINT_DT accounting
  -- trigger never posted (it handles 43/44 only) — the mirror was the ONLY accounting for complaint
  -- revenue, so emitting it is required, not a duplicate. Kept as a switch in case a future FL is
  -- posted on both sides.
  c_skip_complaint_mirrors CONSTANT BOOLEAN := FALSE;

  PROCEDURE set_enabled(p_enabled IN BOOLEAN);
  FUNCTION  is_enabled RETURN BOOLEAN;

  FUNCTION payment_method(p_payment_type_fk IN NUMBER) RETURN VARCHAR2;

  -- The single emission entry point. p_amounts is a JSON object of named amounts
  -- (e.g. {"amount": 250} or {"grossAmount":..,"paidAmount":..,"remainingAmount":..}); p_base_amount
  -- is the signed base amount — this procedure applies ABS and the _REVERSED rule.
  PROCEDURE emit(
    p_source_table     IN VARCHAR2,
    p_source_pk        IN NUMBER,
    p_source_action    IN VARCHAR2,             -- INSERT | UPDATE | DELETE | REPLAY
    p_event_type       IN VARCHAR2,             -- canonical code, without _REVERSED
    p_normal_sign      IN NUMBER DEFAULT 1,     -- +1: positive is the normal case; -1: negative is (FL 55/56/57)
    p_reverse          IN BOOLEAN DEFAULT FALSE,-- force the mirrored entry (old value on UPDATE / DELETE)
    p_doc_date         IN DATE,
    p_base_amount      IN NUMBER,
    p_amounts          IN JSON_OBJECT_T DEFAULT NULL,
    p_org_fk           IN NUMBER,
    p_sub_fk           IN NUMBER,
    p_payment_type_fk  IN NUMBER DEFAULT NULL,
    p_expense_type_id  IN NUMBER DEFAULT NULL,
    p_party_type       IN VARCHAR2 DEFAULT NULL,-- CUSTOMER | SUPPLIER | INVESTOR | EMPLOYEE
    p_party_pk         IN NUMBER DEFAULT NULL,
    p_contract_pk      IN NUMBER DEFAULT NULL,
    p_investor_pk      IN NUMBER DEFAULT NULL,
    p_complaint_pk     IN NUMBER DEFAULT NULL,
    p_contract_no      IN VARCHAR2 DEFAULT NULL, -- D-21: the caller's own number when it IS the contract row
    p_complaint_no     IN VARCHAR2 DEFAULT NULL, -- D-21: the caller's own number when it IS the complaint row
    p_description_ar   IN VARCHAR2 DEFAULT NULL,
    p_description_en   IN VARCHAR2 DEFAULT NULL);

  -- ---- COMPOUND SETTLEMENT (accountant's notes, owner's choice 2026-10-09) ----
  -- A settlement is ONE entry: Dr cash + Dr cheque (that day's payments) + Dr shareholders (discount)
  -- / Cr customers (total). The legacy screens save the day's payments first and the settlement a few
  -- minutes later, so settle() reverses the day's already-posted payment emissions (action MERGED)
  -- and emits one settlement event with amounts {cashAmount, chequeAmount, discountAmount,
  -- totalAmount} (FIN rules: Flyway V44). unsettle() reverses the latest settlement and, when asked,
  -- re-emits the merged payments as ordinary payments again (settlement cancelled).
  -- p_kind: 'CONTRACT' (payments LOAN_PAYMENT FL 7) | 'COMPLAINT' (payments COMPLAINT_DT FL 43).
  PROCEDURE settle(p_kind VARCHAR2, p_pk NUMBER, p_action VARCHAR2, p_discount NUMBER, p_date DATE,
                   p_org NUMBER, p_sub NUMBER, p_cust NUMBER, p_inv NUMBER, p_contract NUMBER,
                   p_no VARCHAR2, p_note VARCHAR2);
  PROCEDURE unsettle(p_kind VARCHAR2, p_pk NUMBER, p_action VARCHAR2, p_restore_payments BOOLEAN);
  -- a payment whose latest emission is MERGED lives inside a settlement entry
  FUNCTION  is_merged(p_source_table VARCHAR2, p_source_pk NUMBER) RETURN BOOLEAN;
  -- an edit of such a payment cannot be posted on its own; it is logged for the accountant
  PROCEDURE flag_merged_edit(p_source_table VARCHAR2, p_source_pk NUMBER, p_action VARCHAR2);

END PKG_ACCOUNTING_EVENT;
/

CREATE OR REPLACE PACKAGE BODY PKG_ACCOUNTING_EVENT AS

  g_enabled BOOLEAN := TRUE;
  -- D-21. Declared before any subprogram body: PL/SQL refuses a declaration after one (PLS-00103,
  -- hit applying D-21 — and a package body that does not compile fails EVERY business save, since
  -- the triggers call it; so this file's verification now stops on an INVALID body).
  e_mutating EXCEPTION;
  PRAGMA EXCEPTION_INIT(e_mutating, -4091);

  PROCEDURE set_enabled(p_enabled IN BOOLEAN) IS BEGIN g_enabled := p_enabled; END;
  FUNCTION  is_enabled RETURN BOOLEAN IS BEGIN RETURN g_enabled; END;

  FUNCTION payment_method(p_payment_type_fk IN NUMBER) RETURN VARCHAR2 IS
  BEGIN
    -- legacy: CASE WHEN account = 24 AND PAYMENT_TYPE_FK = 1 THEN 30 ELSE account END → only 1 is a cheque
    RETURN CASE WHEN p_payment_type_fk = 1 THEN 'CHEQUE' ELSE 'CASH' END;
  END;

  FUNCTION contract_no(p_contract_pk IN NUMBER) RETURN VARCHAR2 IS
    v VARCHAR2(200);
  BEGIN
    IF p_contract_pk IS NULL THEN RETURN NULL; END IF;
    SELECT CONTRACT_AUTO_PK INTO v FROM CONTRACT WHERE CONTRACT_PK = p_contract_pk;
    RETURN v;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN NULL;
    -- D-21: called from a trigger ON the very table being read (or from one a legacy trigger
    -- cascades into while it is mutating) — ORA-04091. The number is informational, never an
    -- accounting input, so fall back to the key rather than lose the whole emission.
    WHEN e_mutating THEN RETURN NULL;
  END;

  FUNCTION complaint_no(p_complaint_pk IN NUMBER) RETURN VARCHAR2 IS
    v VARCHAR2(200);
  BEGIN
    IF p_complaint_pk IS NULL THEN RETURN NULL; END IF;
    SELECT TO_CHAR(COMPLAINT_NUMBER) INTO v FROM COMPLAINTS WHERE COMPLAINT_ID = p_complaint_pk;
    RETURN v;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN NULL;
    -- D-21: called from a trigger ON the very table being read (or from one a legacy trigger
    -- cascades into while it is mutating) — ORA-04091. The number is informational, never an
    -- accounting input, so fall back to the key rather than lose the whole emission.
    WHEN e_mutating THEN RETURN NULL;
  END;

  PROCEDURE log_only(p_ref VARCHAR2, p_type VARCHAR2, p_table VARCHAR2, p_pk NUMBER, p_action VARCHAR2,
                     p_doc_date DATE, p_amount NUMBER, p_payload JSON_OBJECT_T, p_error VARCHAR2) IS
    PRAGMA AUTONOMOUS_TRANSACTION;   -- a failure record must survive even if the caller later rolls back
    v_json CLOB;
  BEGIN
    -- A JSON_OBJECT_T method call cannot appear inside a SQL statement (ORA-40573, found on the
    -- first real emit 2026-09-24) — materialise the value in a local first, then bind the local.
    v_json := p_payload.to_clob();
    INSERT INTO ACCOUNTING_EVENT_LOG (EVENT_REFERENCE, EVENT_TYPE_CODE, SOURCE_TABLE, SOURCE_PK, SOURCE_ACTION,
                                      DOC_DATE, BASE_AMOUNT, PAYLOAD, ENQUEUED_FL, ENQUEUE_ERROR)
    VALUES (p_ref, p_type, p_table, p_pk, p_action, NVL(p_doc_date, SYSDATE), NVL(p_amount, 0),
            v_json, 0, SUBSTR(p_error, 1, 4000));
    COMMIT;
  EXCEPTION WHEN OTHERS THEN
    ROLLBACK;   -- last resort: never propagate into the business save …
    -- … but never vanish either (2026-09-24: a double failure was invisible for an hour)
    DBMS_OUTPUT.PUT_LINE('PKG_ACCOUNTING_EVENT.log_only failed for ' || p_ref || ': ' || SQLERRM || ' | original: ' || SUBSTR(p_error, 1, 300));
  END;

  PROCEDURE emit(
    p_source_table     IN VARCHAR2,
    p_source_pk        IN NUMBER,
    p_source_action    IN VARCHAR2,
    p_event_type       IN VARCHAR2,
    p_normal_sign      IN NUMBER DEFAULT 1,
    p_reverse          IN BOOLEAN DEFAULT FALSE,
    p_doc_date         IN DATE,
    p_base_amount      IN NUMBER,
    p_amounts          IN JSON_OBJECT_T DEFAULT NULL,
    p_org_fk           IN NUMBER,
    p_sub_fk           IN NUMBER,
    p_payment_type_fk  IN NUMBER DEFAULT NULL,
    p_expense_type_id  IN NUMBER DEFAULT NULL,
    p_party_type       IN VARCHAR2 DEFAULT NULL,
    p_party_pk         IN NUMBER DEFAULT NULL,
    p_contract_pk      IN NUMBER DEFAULT NULL,
    p_investor_pk      IN NUMBER DEFAULT NULL,
    p_complaint_pk     IN NUMBER DEFAULT NULL,
    p_contract_no      IN VARCHAR2 DEFAULT NULL, -- D-21: the caller's own number when it IS the contract row
    p_complaint_no     IN VARCHAR2 DEFAULT NULL, -- D-21: the caller's own number when it IS the complaint row
    p_description_ar   IN VARCHAR2 DEFAULT NULL,
    p_description_en   IN VARCHAR2 DEFAULT NULL)
  IS
    v_seq        NUMBER;
    v_ref        VARCHAR2(100);
    v_reverses   VARCHAR2(100);   -- D-16: the emission this undo cancels, when one exists
    v_type       VARCHAR2(50);
    v_amount     NUMBER;
    v_flip       BOOLEAN;
    v_contra     BOOLEAN;
    v_doc_date   DATE;
    v_payload    JSON_OBJECT_T := JSON_OBJECT_T();
    v_fields     JSON_OBJECT_T := JSON_OBJECT_T();
    v_amounts    JSON_OBJECT_T;
    v_json       CLOB;              -- 19c: the envelope as JSON text; bound, never built inline (ORA-40573)
    v_enq_opts   DBMS_AQ.ENQUEUE_OPTIONS_T;
    v_msg_props  DBMS_AQ.MESSAGE_PROPERTIES_T;
    v_msg_id     RAW(16);
    v_undo_fl    NUMBER(1) := CASE WHEN p_reverse THEN 1 ELSE 0 END;   -- 19c: BOOLEAN resolved in PL/SQL
  BEGIN
    IF NOT g_enabled THEN RETURN; END IF;

    v_seq      := ACCOUNTING_EVENT_SEQ.NEXTVAL;
    v_ref      := 'LEGACY:' || p_source_table || ':' || p_source_pk || ':' || v_seq;
    v_doc_date := TRUNC(NVL(p_doc_date, SYSDATE));
    v_amount   := ABS(NVL(p_base_amount, 0));

    -- sign rule (F-10): the entry is mirrored when the amount's sign is not the branch's normal
    -- sign, XOR when the caller asks for the mirror (old value on UPDATE, any DELETE). XOR, not OR
    -- (D-19): the undo of an original that was itself mirrored must post the NORMAL type, or the
    -- undo repeats the original's direction and the row is booked twice instead of cancelled.
    v_contra := SIGN(NVL(p_base_amount, 0)) <> 0 AND SIGN(NVL(p_base_amount, 0)) <> p_normal_sign;
    v_flip   := (p_reverse AND NOT v_contra) OR (NOT p_reverse AND v_contra);
    v_type := p_event_type || CASE WHEN v_flip THEN '_REVERSED' ELSE '' END;

    -- ---- D-16: an UNDO (old value on UPDATE, any DELETE) names the emission it cancels ----
    -- Decided 2026-09-24 (fin-consumer): the consumer posts EVERY message through from-event, an
    -- undo included — the <type>_REVERSED twin rule posts the contra entry under the undo's own
    -- eventReference, so it is idempotent and needs one permission. reversesEventReference is
    -- carried for traceability and audit only; no caller resolves it to FIN's /reverse.
    -- The latest non-undo emission of the same type family on the same source row is the one an
    -- undo cancels; undo emissions themselves (UNDO_FL = 1) are never candidates.
    IF p_reverse THEN
      BEGIN
        SELECT event_reference INTO v_reverses
          FROM (SELECT event_reference FROM ACCOUNTING_EVENT_LOG
                 WHERE source_table = p_source_table AND source_pk = p_source_pk
                   AND enqueued_fl = 1 AND undo_fl = 0
                   AND event_type_code IN (p_event_type, p_event_type || '_REVERSED')
                 ORDER BY event_log_pk DESC)
         WHERE ROWNUM = 1;
      EXCEPTION WHEN NO_DATA_FOUND THEN v_reverses := NULL;
      END;
    END IF;

    -- ---- fields{} — business facts only, keys = FIN_EVENT_BUSINESS_FIELD codes ----
    v_fields.put('PAYMENT_METHOD',    payment_method(p_payment_type_fk));
    IF p_expense_type_id IS NOT NULL THEN v_fields.put('EXPENSE_TYPE_CODE', TO_CHAR(p_expense_type_id)); END IF;
    v_fields.put('ORGANISATION_CODE', TO_CHAR(NVL(p_org_fk, 1)));
    v_fields.put('BRANCH_CODE',       TO_CHAR(p_sub_fk));
    -- references FIN never reads (REQ-FIN-047); kept for the consumer's inbox and reconciliation
    v_fields.put('sourceTable',  p_source_table);
    v_fields.put('sourcePk',     TO_CHAR(p_source_pk));
    v_fields.put('sourceAction', p_source_action);
    v_fields.put('undo', CASE WHEN p_reverse THEN 'Y' ELSE 'N' END);                              -- D-16
    IF v_reverses IS NOT NULL THEN v_fields.put('reversesEventReference', v_reverses); END IF;   -- D-16
    IF p_party_type  IS NOT NULL THEN v_fields.put('partyType', p_party_type); v_fields.put('partyCode', TO_CHAR(p_party_pk)); END IF;
    IF p_contract_pk IS NOT NULL THEN v_fields.put('contractNo',   COALESCE(p_contract_no, contract_no(p_contract_pk), TO_CHAR(p_contract_pk))); END IF;
    IF p_investor_pk IS NOT NULL THEN v_fields.put('investorCode', TO_CHAR(p_investor_pk)); END IF;
    IF p_complaint_pk IS NOT NULL THEN v_fields.put('complaintNo', COALESCE(p_complaint_no, complaint_no(p_complaint_pk), TO_CHAR(p_complaint_pk))); END IF;

    -- ---- amounts{} — absolute values; a caller-supplied object is normalised to ABS ----
    IF p_amounts IS NULL THEN
      v_amounts := JSON_OBJECT_T(); v_amounts.put('amount', v_amount);
    ELSE
      v_amounts := JSON_OBJECT_T();
      DECLARE k JSON_KEY_LIST := p_amounts.get_keys;
      BEGIN
        FOR i IN 1 .. k.COUNT LOOP v_amounts.put(k(i), ABS(NVL(p_amounts.get_number(k(i)), 0))); END LOOP;
      END;
    END IF;

    -- ---- the envelope (API-FIN-020 EventEntryBuildRequest) ----
    v_payload.put('eventReference', v_ref);
    v_payload.put('eventTypeCode',  v_type);
    v_payload.put('occurredAt',     TO_CHAR(SYSTIMESTAMP AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'));
    v_payload.put('docDate',        TO_CHAR(v_doc_date, 'YYYY-MM-DD'));
    v_payload.put('baseAmount',     v_amount);
    v_payload.put('amounts',        v_amounts);
    v_payload.put('fields',         v_fields);
    v_payload.put('descriptionAr',  SUBSTR(NVL(p_description_ar, v_type), 1, 500));
    v_payload.put('descriptionEn',  SUBSTR(NVL(p_description_en, v_type), 1, 500));

    IF v_amount = 0 THEN
      log_only(v_ref, v_type, p_source_table, p_source_pk, p_source_action, v_doc_date, 0, v_payload, 'ZERO_AMOUNT_SKIPPED');
      RETURN;
    END IF;

    -- ---- log (same transaction as the business row) then enqueue ----
    v_json := v_payload.to_clob();
    INSERT INTO ACCOUNTING_EVENT_LOG (EVENT_REFERENCE, EVENT_TYPE_CODE, SOURCE_TABLE, SOURCE_PK, SOURCE_ACTION,
                                      DOC_DATE, BASE_AMOUNT, PAYLOAD, ENQUEUED_FL, UNDO_FL, REVERSES_EVENT_REFERENCE)
    VALUES (v_ref, v_type, p_source_table, p_source_pk, p_source_action, v_doc_date, v_amount, v_json, 1,
            v_undo_fl, v_reverses);   -- 19c: no BOOLEAN inside SQL (ORA-00920, prod 2026-09-25)

    v_msg_props.correlation := v_ref;                 -- lets the consumer / DBA find a message by reference
    v_msg_props.exception_queue := c_exception_queue; -- D-20: past max_retries it moves there, not limbo
    v_enq_opts.visibility   := DBMS_AQ.ON_COMMIT;     -- the event exists only if the business save commits
    DBMS_AQ.ENQUEUE(queue_name         => c_queue,
                    enqueue_options    => v_enq_opts,
                    message_properties => v_msg_props,
                    payload            => ACCOUNTING_EVENT_MSG_T(v_json),   -- 19c classic AQ object payload
                    msgid              => v_msg_id);
  EXCEPTION
    WHEN OTHERS THEN
      -- never break the business save; the log row (autonomous) is the replay hook
      log_only(NVL(v_ref, 'LEGACY:' || p_source_table || ':' || p_source_pk || ':ERR' || TO_CHAR(SYSTIMESTAMP, 'HH24MISSFF3')),
               NVL(v_type, p_event_type), p_source_table, p_source_pk, p_source_action, v_doc_date, v_amount,
               v_payload, SQLERRM || ' @ ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE);
  END emit;

  -- ======================================================================================
  -- COMPOUND SETTLEMENT (2026-10-09) — see the spec
  -- ======================================================================================
  FUNCTION latest_action(p_source_table VARCHAR2, p_source_pk NUMBER, p_undo OUT NUMBER) RETURN VARCHAR2 IS
    v_action VARCHAR2(10);
  BEGIN
    SELECT source_action, undo_fl INTO v_action, p_undo FROM (
      SELECT source_action, undo_fl FROM ACCOUNTING_EVENT_LOG
       WHERE source_table = p_source_table AND source_pk = p_source_pk AND enqueued_fl = 1
       ORDER BY event_log_pk DESC) WHERE ROWNUM = 1;
    RETURN v_action;
  EXCEPTION WHEN NO_DATA_FOUND THEN p_undo := NULL; RETURN NULL;
  END;

  FUNCTION is_merged(p_source_table VARCHAR2, p_source_pk NUMBER) RETURN BOOLEAN IS
    v_undo NUMBER;
  BEGIN
    RETURN NVL(latest_action(p_source_table, p_source_pk, v_undo) = 'MERGED', FALSE);
  END;

  -- the payment's latest emission is an ordinary posted one (not an undo, not merged)
  FUNCTION is_posted(p_source_table VARCHAR2, p_source_pk NUMBER) RETURN BOOLEAN IS
    v_undo NUMBER; v_action VARCHAR2(10);
  BEGIN
    v_action := latest_action(p_source_table, p_source_pk, v_undo);
    RETURN v_action IS NOT NULL AND v_action <> 'MERGED' AND v_undo = 0;
  END;

  PROCEDURE flag_merged_edit(p_source_table VARCHAR2, p_source_pk NUMBER, p_action VARCHAR2) IS
    v_p JSON_OBJECT_T := JSON_OBJECT_T();
  BEGIN
    v_p.put('sourceTable', p_source_table); v_p.put('sourcePk', TO_CHAR(p_source_pk)); v_p.put('sourceAction', p_action);
    log_only('LEGACY:' || p_source_table || ':' || p_source_pk || ':' || ACCOUNTING_EVENT_SEQ.NEXTVAL, 'MERGED_PAYMENT_EDIT',
             p_source_table, p_source_pk, p_action, SYSDATE, 0, v_p,
             'MERGED_INTO_SETTLEMENT: this payment is inside a compound settlement entry; its ' || p_action
             || ' was not posted - the accountant adjusts the settlement with a manual entry');
  END;

  -- (re)emit one payment row exactly as its trigger does
  PROCEDURE emit_payment(p_kind VARCHAR2, p_pay_pk NUMBER, p_action VARCHAR2, p_reverse BOOLEAN, p_no VARCHAR2) IS
  BEGIN
    IF p_kind = 'CONTRACT' THEN
      FOR r IN (SELECT * FROM LOAN_PAYMENT WHERE LOAN_PAYMENT_PK = p_pay_pk) LOOP
        emit(p_source_table => 'LOAN_PAYMENT', p_source_pk => r.LOAN_PAYMENT_PK, p_source_action => p_action,
             p_event_type => 'INSTALLMENT_PAYMENT_RECEIVED', p_normal_sign => 1, p_reverse => p_reverse,
             p_doc_date => r.LOAN_PAYMENT_DATE, p_base_amount => r.LOAN_PAYMENT_VALUE,
             p_org_fk => r.ORGANIZATION_FK, p_sub_fk => r.ORGANIZATION_SUB_FK, p_payment_type_fk => r.PAYMENT_TYPE_FK,
             p_party_type => CASE WHEN r.SUPPLIERS_FK IS NOT NULL THEN 'SUPPLIER' WHEN r.CUSTOMERS_FK IS NOT NULL THEN 'CUSTOMER'
                                  WHEN r.INVESTOR_FK IS NOT NULL THEN 'INVESTOR' END,
             p_party_pk => COALESCE(r.SUPPLIERS_FK, r.CUSTOMERS_FK, r.INVESTOR_FK),
             p_contract_pk => r.CONTRACT_FK, p_investor_pk => r.INVESTOR_FK, p_complaint_pk => r.COMPLAINT_FK,
             p_contract_no => p_no,
             p_description_ar => 'قيد الى لسداد عميل ' || r.NOTE,
             p_description_en => 'INSTALLMENT_PAYMENT_RECEIVED ' || r.NOTE);
      END LOOP;
    ELSE
      FOR r IN (SELECT * FROM COMPLAINT_DT WHERE COMPLAINT_DT_PK = p_pay_pk) LOOP
        emit(p_source_table => 'COMPLAINT_DT', p_source_pk => r.COMPLAINT_DT_PK, p_source_action => p_action,
             p_event_type => 'COURT_INSTALLMENT_PAYMENT', p_normal_sign => 1, p_reverse => p_reverse,
             p_doc_date => NVL(r.COMPLAINT_DT_DATE, SYSDATE), p_base_amount => r.VALUE,
             p_org_fk => r.ORGANIZATION_FK, p_sub_fk => r.ORGANIZATION_SUB_FK, p_payment_type_fk => r.PAYMENT_TYPE_FK,
             p_contract_pk => r.CONTRACT_FK, p_complaint_pk => r.COMPLAINT_FK, p_complaint_no => p_no,
             p_description_ar => 'قيد الى ناتج عن سداد شكوى ' || r.NOTS,
             p_description_en => 'COURT_INSTALLMENT_PAYMENT');
      END LOOP;
    END IF;
  END;

  PROCEDURE settle(p_kind VARCHAR2, p_pk NUMBER, p_action VARCHAR2, p_discount NUMBER, p_date DATE,
                   p_org NUMBER, p_sub NUMBER, p_cust NUMBER, p_inv NUMBER, p_contract NUMBER,
                   p_no VARCHAR2, p_note VARCHAR2) IS
    v_pay_tab VARCHAR2(30) := CASE p_kind WHEN 'CONTRACT' THEN 'LOAN_PAYMENT' ELSE 'COMPLAINT_DT' END;
    v_type    VARCHAR2(50) := CASE p_kind WHEN 'CONTRACT' THEN 'CONTRACT_SETTLEMENT' ELSE 'COMPLAINT_SETTLEMENT' END;
    v_date    DATE := TRUNC(NVL(p_date, SYSDATE));
    v_cash    NUMBER := 0;
    v_cheque  NUMBER := 0;
    v_pt      NUMBER;                 -- the ONE payment method the settlement event carries (V45)
    v_paid    NUMBER := 0;
    v_disc    NUMBER := ABS(NVL(p_discount, 0));
    v_total   NUMBER;
    v_amounts JSON_OBJECT_T := JSON_OBJECT_T();
    TYPE t_pay IS RECORD (pk NUMBER, v NUMBER, pt NUMBER);
    TYPE t_pays IS TABLE OF t_pay;
    v_pays    t_pays;
    PROCEDURE take(p_pay_pk NUMBER, p_value NUMBER) IS
    BEGIN
      IF NOT is_merged(v_pay_tab, p_pay_pk) THEN
        IF NOT is_posted(v_pay_tab, p_pay_pk) THEN RETURN; END IF;     -- never posted: nothing to absorb
        emit_payment(p_kind, p_pay_pk, 'MERGED', TRUE, p_no);           -- cancel its own entry
        IF NOT is_merged(v_pay_tab, p_pay_pk) THEN RETURN; END IF;     -- the cancel failed (logged): leave it standalone
      END IF;
      v_paid := v_paid + ABS(p_value);
    END;
  BEGIN
    IF NOT g_enabled THEN RETURN; END IF;
    IF p_kind = 'CONTRACT' THEN
      SELECT LOAN_PAYMENT_PK, LOAN_PAYMENT_VALUE, CASE WHEN PAYMENT_TYPE_FK = 1 THEN 1 ELSE 0 END BULK COLLECT INTO v_pays
        FROM LOAN_PAYMENT WHERE CONTRACT_FK = p_pk AND LOAN_PAYMENT_FL = 7 AND TRUNC(LOAN_PAYMENT_DATE) = v_date
         AND NVL(LOAN_PAYMENT_VALUE, 0) > 0 ORDER BY LOAN_PAYMENT_PK;
    ELSE
      SELECT COMPLAINT_DT_PK, VALUE, CASE WHEN PAYMENT_TYPE_FK = 1 THEN 1 ELSE 0 END BULK COLLECT INTO v_pays
        FROM COMPLAINT_DT WHERE COMPLAINT_FK = p_pk AND COMPLAINT_DT_FL = 43 AND TRUNC(COMPLAINT_DT_DATE) = v_date
         AND NVL(VALUE, 0) > 0 ORDER BY COMPLAINT_DT_PK;
    END IF;
    -- one method per settlement (the FIN rule maps PAYMENT_METHOD): the larger one is absorbed; on the
    -- rare mixed day the other method's payments keep their own entries (none in 2026)
    FOR i IN 1 .. v_pays.COUNT LOOP
      IF is_merged(v_pay_tab, v_pays(i).pk) OR is_posted(v_pay_tab, v_pays(i).pk) THEN
        IF v_pays(i).pt = 1 THEN v_cheque := v_cheque + v_pays(i).v; ELSE v_cash := v_cash + v_pays(i).v; END IF;
      END IF;
    END LOOP;
    v_pt := CASE WHEN v_cheque > v_cash THEN 1 ELSE 0 END;
    FOR i IN 1 .. v_pays.COUNT LOOP
      IF v_pays(i).pt = v_pt THEN take(v_pays(i).pk, v_pays(i).v); END IF;
    END LOOP;
    v_total := v_paid + v_disc;
    IF v_total = 0 THEN RETURN; END IF;
    v_amounts.put('paidAmount', v_paid);
    v_amounts.put('discountAmount', v_disc);
    v_amounts.put('totalAmount', v_total);
    emit(p_source_table => CASE p_kind WHEN 'CONTRACT' THEN 'CONTRACT' ELSE 'COMPLAINTS' END,
         p_source_pk => p_pk, p_source_action => p_action, p_event_type => v_type, p_normal_sign => 1, p_reverse => FALSE,
         p_doc_date => v_date, p_base_amount => v_total, p_amounts => v_amounts, p_payment_type_fk => v_pt,
         p_org_fk => p_org, p_sub_fk => p_sub,
         p_party_type => 'CUSTOMER', p_party_pk => p_cust, p_contract_pk => p_contract, p_investor_pk => p_inv,
         p_complaint_pk => CASE WHEN p_kind = 'COMPLAINT' THEN p_pk END,
         p_contract_no  => CASE WHEN p_kind = 'CONTRACT' THEN p_no END,
         p_complaint_no => CASE WHEN p_kind = 'COMPLAINT' THEN p_no END,
         p_description_ar => CASE p_kind WHEN 'CONTRACT' THEN 'قيد مركب - مخالصة عقد رقم ' ELSE 'قيد مركب - مخالصة شكوى رقم ' END
                             || p_no || ' ' || p_note,
         p_description_en => v_type || ' (compound) ' || p_no);
  END settle;

  PROCEDURE unsettle(p_kind VARCHAR2, p_pk NUMBER, p_action VARCHAR2, p_restore_payments BOOLEAN) IS
    v_tab   VARCHAR2(30) := CASE p_kind WHEN 'CONTRACT' THEN 'CONTRACT' ELSE 'COMPLAINTS' END;
    v_type  VARCHAR2(50) := CASE p_kind WHEN 'CONTRACT' THEN 'CONTRACT_SETTLEMENT' ELSE 'COMPLAINT_SETTLEMENT' END;
    v_payload CLOB; v_date DATE; v_no VARCHAR2(200); v_pt NUMBER := 0;
    v_obj JSON_OBJECT_T; v_am JSON_OBJECT_T; v_f JSON_OBJECT_T; v_am2 JSON_OBJECT_T := JSON_OBJECT_T();
  BEGIN
    IF NOT g_enabled THEN RETURN; END IF;
    BEGIN
      SELECT payload, doc_date INTO v_payload, v_date FROM (
        SELECT l.payload, l.doc_date FROM ACCOUNTING_EVENT_LOG l
         WHERE l.source_table = v_tab AND l.source_pk = p_pk AND l.enqueued_fl = 1 AND l.undo_fl = 0
           AND l.event_type_code IN (v_type, v_type || '_REVERSED')
           AND NOT EXISTS (SELECT 1 FROM ACCOUNTING_EVENT_LOG u
                            WHERE u.reverses_event_reference = l.event_reference AND u.enqueued_fl = 1)
         ORDER BY l.event_log_pk DESC) WHERE ROWNUM = 1;
      v_obj := JSON_OBJECT_T.parse(v_payload);
      v_am  := v_obj.get_object('amounts');
      v_f   := v_obj.get_object('fields');
      v_no  := COALESCE(v_f.get_string('contractNo'), v_f.get_string('complaintNo'));
      -- the reversal is posted with today's rule (V45: paidAmount + PAYMENT_METHOD), whatever shape
      -- the original settlement had
      IF v_am.has('paidAmount') THEN                       -- V45 shape
        v_am2 := v_am;
        v_pt := CASE WHEN v_f.get_string('PAYMENT_METHOD') = 'CHEQUE' THEN 1 ELSE 0 END;
      ELSIF v_am.has('cashAmount') THEN                    -- V44 shape (cash + cheque)
        v_am2.put('paidAmount', v_am.get_number('cashAmount') + v_am.get_number('chequeAmount'));
        v_am2.put('discountAmount', v_am.get_number('discountAmount'));
        v_am2.put('totalAmount', v_am.get_number('totalAmount'));
        v_pt := CASE WHEN v_am.get_number('chequeAmount') > 0 THEN 1 ELSE 0 END;
      ELSE                                                 -- before V44: two lines, {amount}
        v_am2.put('paidAmount', 0);
        v_am2.put('discountAmount', v_am.get_number('amount')); v_am2.put('totalAmount', v_am.get_number('amount'));
      END IF;
      emit(p_source_table => v_tab, p_source_pk => p_pk, p_source_action => p_action, p_event_type => v_type,
           p_normal_sign => 1, p_reverse => TRUE, p_doc_date => v_date,
           p_base_amount => v_am2.get_number('totalAmount'), p_amounts => v_am2, p_payment_type_fk => v_pt,
           p_org_fk => TO_NUMBER(v_f.get_string('ORGANISATION_CODE')), p_sub_fk => TO_NUMBER(v_f.get_string('BRANCH_CODE')),
           p_party_type => 'CUSTOMER', p_party_pk => TO_NUMBER(v_f.get_string('partyCode')),
           p_contract_pk  => CASE WHEN p_kind = 'CONTRACT' THEN p_pk END,
           p_complaint_pk => CASE WHEN p_kind = 'COMPLAINT' THEN p_pk END,
           p_contract_no  => CASE WHEN p_kind = 'CONTRACT' THEN v_no END,
           p_complaint_no => CASE WHEN p_kind = 'COMPLAINT' THEN v_no END,
           p_description_ar => 'عكس قيد مركب - مخالصة رقم ' || v_no,
           p_description_en => v_type || ' (compound) reversed ' || v_no);
    EXCEPTION WHEN NO_DATA_FOUND THEN NULL;  -- nothing posted to undo
    END;
    IF p_restore_payments THEN               -- settlement cancelled: the merged payments stand alone again
      IF p_kind = 'CONTRACT' THEN
        FOR r IN (SELECT LOAN_PAYMENT_PK pk FROM LOAN_PAYMENT WHERE CONTRACT_FK = p_pk AND LOAN_PAYMENT_FL = 7) LOOP
          IF is_merged('LOAN_PAYMENT', r.pk) THEN emit_payment('CONTRACT', r.pk, 'UPDATE', FALSE, v_no); END IF;
        END LOOP;
      ELSE
        FOR r IN (SELECT COMPLAINT_DT_PK pk FROM COMPLAINT_DT WHERE COMPLAINT_FK = p_pk AND COMPLAINT_DT_FL = 43) LOOP
          IF is_merged('COMPLAINT_DT', r.pk) THEN emit_payment('COMPLAINT', r.pk, 'UPDATE', FALSE, v_no); END IF;
        END LOOP;
      END IF;
    END IF;
  END unsettle;

END PKG_ACCOUNTING_EVENT;
/

SHOW ERRORS PACKAGE PKG_ACCOUNTING_EVENT
SHOW ERRORS PACKAGE BODY PKG_ACCOUNTING_EVENT

-- STOP HERE IF THE PACKAGE IS NOT VALID. "Package body created with compilation errors" is only a
-- warning to sqlplus — WHENEVER SQLERROR does not fire — yet with file 3 installed EVERY insert,
-- update and delete on the 7 business tables now fails for the users (the triggers call this
-- package). Seen on the test copy 2026-09-24 (D-21, PLS-00103), fixed within a minute.
-- EMERGENCY on a live system: disable the 7 TRG_AE_* triggers (file 3's rollback companion) —
-- users save again at once, emissions stop, and ACCOUNTING_EVENT_LOG/replay covers the gap.
DECLARE n NUMBER;
BEGIN
  SELECT COUNT(*) INTO n FROM user_objects WHERE object_name = 'PKG_ACCOUNTING_EVENT' AND status <> 'VALID';
  IF n > 0 THEN
    RAISE_APPLICATION_ERROR(-20001, 'PKG_ACCOUNTING_EVENT is INVALID - business saves on the 7 tables will fail. '
      || 'Disable the TRG_AE_* triggers now (file 3 rollback companion), then fix and re-run this file.');
  END IF;
END;
/

-- Re-running this file on a live system (the D-19/D-20 fixes did) recompiles the SPEC, which
-- invalidates file 3's TRG_AE_* triggers. Oracle recompiles an invalid trigger on its next fire, but
-- a live table must not depend on that — recompile them here, now. No-op on the first run.
BEGIN
  FOR t IN (SELECT object_name FROM user_objects
             WHERE object_type = 'TRIGGER' AND object_name LIKE 'TRG_AE\_%' ESCAPE '\' AND status = 'INVALID') LOOP
    EXECUTE IMMEDIATE 'ALTER TRIGGER ' || t.object_name || ' COMPILE';
    DBMS_OUTPUT.PUT_LINE('recompiled ' || t.object_name);
  END LOOP;
END;
/

-- ----------------------------------------------------------------------------
-- Verification (read-only)
-- ----------------------------------------------------------------------------
SELECT object_name, object_type, status FROM user_objects WHERE object_name = 'PKG_ACCOUNTING_EVENT';
-- expected: PACKAGE VALID, PACKAGE BODY VALID
SELECT object_name, status FROM user_objects WHERE object_name LIKE 'TRG_AE\_%' ESCAPE '\' ORDER BY 1;
-- expected (once file 3 has run): 7 rows, all VALID
