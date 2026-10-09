-- ============================================================
-- V44 — FIN — customer settlements as ONE compound entry (accountant's notes, 2026-10-08/09)
-- ============================================================
-- «في حالة أي سداد للعملاء سواء مخالصة شكوى أو مخالصة عقد ... قيد مركب»:
--     Dr bank / cash (what was collected)  +  Dr shareholders' current (the discount)
--         / Cr customers (the total)
-- The owner chose the automatic merge (2026-10-09): on the settlement day LOAN_SYS (TRG_AE_CONTRACT /
-- TRG_AE_COMPLAINTS through PKG_ACCOUNTING_EVENT.merge_settlement) reverses that day's payment
-- entries of the contract / complaint and emits ONE settlement event carrying four amounts:
--     cashAmount (payments in cash) · chequeAmount (payments by cheque) · discountAmount · totalAmount
-- so CONTRACT_SETTLEMENT and COMPLAINT_SETTLEMENT become four-line rules. A zero line is omitted by
-- the engine (D-22), e.g. a settlement with no same-day payment posts only Dr 210201 / Cr 110201.
-- Cash/cheque accounts are the PAYMENT_METHOD mapping's own targets (CASH 110101, CHEQUE 110111).
-- Every line keeps the ORG / BRANCH dimensions from the event fields. The _REVERSED twins mirror.
-- Never edit this file once applied.
-- ============================================================

CREATE TEMP TABLE v44_lines (event_type_code VARCHAR(60), line_no INT, direction_code VARCHAR(10),
                             account_code VARCHAR(30), amount_field VARCHAR(60)) ON COMMIT DROP;
INSERT INTO v44_lines
SELECT t.code || CASE WHEN tw.twin THEN '_REVERSED' ELSE '' END, l.line_no,
       CASE WHEN tw.twin THEN CASE l.dir WHEN 'DEBIT' THEN 'CREDIT' ELSE 'DEBIT' END ELSE l.dir END,
       l.acct, l.fld
  FROM (VALUES ('CONTRACT_SETTLEMENT'), ('COMPLAINT_SETTLEMENT')) t(code)
 CROSS JOIN (VALUES (FALSE), (TRUE)) tw(twin)
 CROSS JOIN (VALUES (1, 'DEBIT',  '110101', 'cashAmount'),
                    (2, 'DEBIT',  '110111', 'chequeAmount'),
                    (3, 'DEBIT',  '210201', 'discountAmount'),
                    (4, 'CREDIT', '110201', 'totalAmount')) l(line_no, dir, acct, fld);

DELETE FROM fin_rule_line_dim d
 USING fin_rule_line l, fin_event_type_rule r
 WHERE d.rule_line_id = l.rule_line_pk AND l.event_type_rule_id = r.event_type_rule_pk
   AND r.event_type_code IN (SELECT DISTINCT event_type_code FROM v44_lines);
DELETE FROM fin_rule_line l
 USING fin_event_type_rule r
 WHERE l.event_type_rule_id = r.event_type_rule_pk
   AND r.event_type_code IN (SELECT DISTINCT event_type_code FROM v44_lines);

INSERT INTO fin_rule_line (rule_line_pk, event_type_rule_id, line_no, account_derivation_type_code,
                           account_derivation_value, account_business_field_code, amount_source_type_code,
                           amount_source_value, direction_code, distribution_type_code, is_remainder_fl, created_at)
SELECT nextval('seq_fin_rule_line'), r.event_type_rule_pk, v.line_no, 'CONSTANT', v.account_code, NULL,
       'FIELD', v.amount_field, v.direction_code, 'FIXED', FALSE, now()
  FROM v44_lines v JOIN fin_event_type_rule r ON r.event_type_code = v.event_type_code;

INSERT INTO fin_rule_line_dim (rule_line_dimension_pk, value_source_code, business_field_code, rule_line_id,
                               dimension_id, dimension_value_id, created_at)
SELECT nextval('seq_fin_rule_line_dim'), 'BUSINESS_FIELD', d.field, l.rule_line_pk, dm.dimension_pk, NULL, now()
  FROM fin_rule_line l
  JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
  JOIN (VALUES ('ORG', 'ORGANISATION_CODE'), ('BRANCH', 'BRANCH_CODE')) d(dim, field) ON TRUE
  JOIN fin_dimension dm ON dm.code = d.dim
 WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v44_lines);

DO $$
DECLARE n INT;
BEGIN
  SELECT count(*) INTO n FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
   WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v44_lines);
  IF n <> 16 THEN RAISE EXCEPTION 'V44: expected 16 settlement rule lines (4 rules x 4), found %', n; END IF;
  SELECT count(*) INTO n FROM fin_rule_line_dim d JOIN fin_rule_line l ON l.rule_line_pk = d.rule_line_id
    JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
   WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v44_lines);
  IF n <> 32 THEN RAISE EXCEPTION 'V44: expected 32 dimension tags, found %', n; END IF;
  SELECT count(*) INTO n FROM v44_lines v
   WHERE NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = v.account_code AND a.is_leaf_fl AND a.is_active_fl);
  IF n > 0 THEN RAISE EXCEPTION 'V44: % settlement lines name a missing / non-postable account', n; END IF;
END $$;
