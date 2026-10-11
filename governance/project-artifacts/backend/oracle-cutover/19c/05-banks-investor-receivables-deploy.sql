-- ============================================================================
-- 05-banks-investor-receivables-deploy.sql — V46 cutover, as APPLIED on production 2026-10-10
-- ============================================================================
-- Approved by the accountant 2026-10-10: bank accounts per LOAN_SYS BANK_ACCOUNT, customers held for
-- investors (210203), owner drawings. Applied directly on the databases — not through Flyway: the
-- system is live (owner, 2026-10-10). The flyway_schema_history row for V46 was inserted so a Flyway
-- start would not re-run it.
--
-- ORDER (each step verified before the next)
--   1. FIN     V46__fin_investor_receivables_bank_accounts_owner_drawing.sql (rules, 210203,
--              BANK_<id> mappings, OWNER_DRAWING) — checksum 1100849283, installed_rank 43
--   2. LOAN_SYS 02-package-19c.sql (v5: payment_method(pt, bank), p_bank_account_fk, settle by
--              method key, unsettle decode, republish) — compiled first as PKG_AE_V46_CHECK, VALID
--   3. LOAN_SYS 03-triggers-19c.sql — 5 triggers re-created DISABLED → VALID → ENABLE, one at a time
--              (LOAN_PAYMENT, COMPLAINT_DT, EXPENSE_TYPE_DT, INVOICE_IMPORT, TRANSFER_SAFE); CONTRACT and
--              COMPLAINTS recompiled. No business row lost an event in the window (checked).
--   4. OCTOBER RE-POST — owner's choice: clean entries, no reversal entries:
--      a. LOAN_SYS: work list AE_V46_REQUEUE = the live October emissions (not undone, not MERGED,
--         doc_date >= 2026-10-01): 154. The 8 emission/undo pairs + the merge undo net to zero and
--         are NOT re-posted (log kept as history).
--      b. FIN: the 172 October EVENT_GENERATED entries deleted (lines, line dims, entries).
--      c. LOAN_SYS: each of the 154 payloads updated in ACCOUNTING_EVENT_LOG (PAYMENT_METHOD →
--         BANK_<id> from the source row, V44 settlement → paidAmount shape, fields.requeued) and
--         re-enqueued under its SAME event reference (block below). fin-consumer posted all 154:
--         JV-2026-000367 .. 000520.
--   5. VERIFIED: 460 expected lines = 460 posted lines (rule × payload, account, side, amount), 0
--      unbalanced entries, 0 lines without a branch.
-- ============================================================================

-- 4a
CREATE TABLE AE_V46_REQUEUE AS
  SELECT l.event_log_pk, l.event_reference, l.event_type_code, l.source_table, l.source_pk,
         CAST(NULL AS VARCHAR2(30)) old_method, CAST(NULL AS VARCHAR2(30)) new_method, CAST(NULL AS DATE) requeued_at
    FROM ACCOUNTING_EVENT_LOG l
   WHERE l.enqueued_fl = 1 AND l.undo_fl = 0 AND l.source_action <> 'MERGED' AND l.doc_date >= DATE '2026-10-01'
     AND NOT EXISTS (SELECT 1 FROM ACCOUNTING_EVENT_LOG u WHERE u.reverses_event_reference = l.event_reference AND u.enqueued_fl = 1);

-- 4b (Postgres, FIN)
-- DELETE fin_journal_line_dim / fin_journal_line / fin_journal_entry
--  WHERE journal_type_code = 'EVENT_GENERATED' AND doc_date >= DATE '2026-10-01'   (172 entries, checked first)

-- 4c
DECLARE
  v_obj  JSON_OBJECT_T; v_f JSON_OBJECT_T; v_am JSON_OBJECT_T; v_am2 JSON_OBJECT_T;
  v_old  VARCHAR2(30); v_new VARCHAR2(30); v_bank NUMBER; v_json CLOB;
  v_enq_opts DBMS_AQ.ENQUEUE_OPTIONS_T; v_msg_props DBMS_AQ.MESSAGE_PROPERTIES_T; v_msg_id RAW(16);
  FUNCTION bank_of(p_table VARCHAR2, p_pk NUMBER) RETURN NUMBER IS v NUMBER;
  BEGIN
    CASE p_table
      WHEN 'LOAN_PAYMENT'    THEN SELECT BANK_ACCOUNT_FK INTO v FROM LOAN_PAYMENT    WHERE LOAN_PAYMENT_PK    = p_pk;
      WHEN 'COMPLAINT_DT'    THEN SELECT BANK_ACCOUNT_FK INTO v FROM COMPLAINT_DT    WHERE COMPLAINT_DT_PK    = p_pk;
      WHEN 'EXPENSE_TYPE_DT' THEN SELECT BANK_ACCOUNT_FK INTO v FROM EXPENSE_TYPE_DT WHERE EXPENSE_TYPE_DT_ID = p_pk;
      WHEN 'INVOICE_IMPORT'  THEN SELECT BANK_ACCOUNT_FK INTO v FROM INVOICE_IMPORT  WHERE INVOICE_IMPORT_PK  = p_pk;
      WHEN 'TRANSFER_SAFE'   THEN SELECT CASE TRANSFER_SAFE_TYPE_FK WHEN 25 THEN FROM_BANK_ACCOUNT_FK ELSE TO_BANK_ACCOUNT_FK END INTO v FROM TRANSFER_SAFE WHERE TRANSFER_SAFE_PK = p_pk;
      WHEN 'COMPLAINTS'      THEN SELECT MAX(BANK_ACCOUNT_FK) INTO v FROM COMPLAINT_DT WHERE COMPLAINT_FK = p_pk AND COMPLAINT_DT_FL = 43 AND PAYMENT_TYPE_FK = 1;
      WHEN 'CONTRACT'        THEN SELECT MAX(BANK_ACCOUNT_FK) INTO v FROM LOAN_PAYMENT WHERE CONTRACT_FK = p_pk AND LOAN_PAYMENT_FL = 7 AND PAYMENT_TYPE_FK = 1;
      ELSE v := NULL;
    END CASE;
    RETURN v;
  EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
  END;
BEGIN
  FOR w IN (SELECT q.ROWID rid, l.ROWID lrid, l.payload, l.source_table, l.source_pk, l.event_reference
              FROM AE_V46_REQUEUE q JOIN ACCOUNTING_EVENT_LOG l ON l.event_log_pk = q.event_log_pk
             ORDER BY q.event_log_pk) LOOP
    v_obj := JSON_OBJECT_T.parse(w.payload);
    v_f   := v_obj.get_object('fields');
    v_am  := v_obj.get_object('amounts');
    v_old := v_f.get_string('PAYMENT_METHOD');
    v_new := v_old;
    IF v_am.has('cashAmount') THEN
      v_am2 := JSON_OBJECT_T();
      v_am2.put('paidAmount',     v_am.get_number('cashAmount') + v_am.get_number('chequeAmount'));
      v_am2.put('discountAmount', v_am.get_number('discountAmount'));
      v_am2.put('totalAmount',    v_am.get_number('totalAmount'));
      v_obj.put('amounts', v_am2);
      IF v_am.get_number('chequeAmount') > 0 THEN v_new := 'CHEQUE'; END IF;
    END IF;
    IF v_new = 'CHEQUE' THEN
      v_bank := bank_of(w.source_table, w.source_pk);
      v_new  := PKG_ACCOUNTING_EVENT.payment_method(1, v_bank);
    END IF;
    v_f.put('PAYMENT_METHOD', v_new);
    v_f.put('requeued', 'V46 2026-10-10');
    v_obj.put('fields', v_f);
    v_json := v_obj.to_clob();
    UPDATE ACCOUNTING_EVENT_LOG SET payload = v_json WHERE ROWID = w.lrid;
    v_msg_props.correlation     := w.event_reference;
    v_msg_props.exception_queue := 'ACCOUNTING_EVENT_Q_EXCPT';
    v_enq_opts.visibility       := DBMS_AQ.ON_COMMIT;
    DBMS_AQ.ENQUEUE(queue_name => 'ACCOUNTING_EVENT_Q', enqueue_options => v_enq_opts, message_properties => v_msg_props,
                    payload => ACCOUNTING_EVENT_MSG_T(v_json), msgid => v_msg_id);
    UPDATE AE_V46_REQUEUE SET old_method = v_old, new_method = v_new, requeued_at = SYSDATE WHERE ROWID = w.rid;
  END LOOP;
  COMMIT;
END;
/
-- result: CASH 23 · CASH→BANK_61 1 (the compound settlement) · CHEQUE→BANK_21 19 · →BANK_61 63 ·
--         →BANK_62 6 · CHEQUE (no bank chosen, → commercial bank) 42

-- ============================================================================
-- 2026-10-10 (later) — APPLIED on production
-- 6. Customers' opening balance 30-09-2026 (accountant: "do it, correct later if wrong"): per contract
--    CASH_TOTAL_CONTRACT − LOAN_PAYMENT FL7 − COMPLAINT_DT FL43 dated < 2026-10-01; contracts settled
--    (FL13) / complaint-settled (FL45) before October = 0. Matches CONTRACT.REMAINING on all 569 live
--    contracts. Posted OPENING entries Dr 110201 / Cr 210203 (ORG 1 + BRANCH):
--      branch 1 أفضل وسيط 1,109,016.419 · branch 2 الوسيط الذهبي 1,714,091.192 · branch 3 الوسائط الفضية 443,292.845
-- 7. V47 (checksum 294823068, flyway row 44): LAWYER_FEE_PAYMENT credited by fields.LAWYER_CODE —
--    41 خالد الظفيري (held; 2026: 6,388 collected, 0 paid) → 211001 مخصص محاماة; 138/145/40/NONE → 4403.
--    PKG v6 (p_lawyer_code) + TRG_AE_COMPLAINT_DT (reads COMPLAINTS.LAWYER_NAME_FK for FL 44).
--    The 13 live October lawyer-fee events: FIN entries deleted, payloads given LAWYER_CODE and
--    re-enqueued under the same references → 6 × 211001 (773.956) + 7 × 4403 (1,463.257).
-- ============================================================================
