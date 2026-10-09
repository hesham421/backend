-- ============================================================
-- V45 — FIN — compound settlements take bank / cash from the PAYMENT_METHOD mapping (fix to V44)
-- ============================================================
-- V44 hard-coded the cash and cheque accounts (110101 / 110111) in the settlement rules, so a change
-- of the PAYMENT_METHOD mapping (e.g. cheques moved to another bank) would not reach settlements —
-- every other payment rule resolves that account through FIN_ACCOUNT_MAPPING (owner, 2026-10-09).
-- Now, like the payment rules:
--     1  DEBIT   MAPPING(PAYMENT_METHOD)  paidAmount       (the day's payments absorbed)
--     2  DEBIT   210201 shareholders      discountAmount
--     3  CREDIT  110201 customers         totalAmount
-- The settlement event carries ONE payment method: in 2026 no settlement day mixed cash and cheque
-- (21 contract + 10 complaint settlements measured on LOAN_SYS). If a day ever does, LOAN_SYS merges
-- the larger method's payments and leaves the others as their own entries (PKG_ACCOUNTING_EVENT.settle).
-- The PAYMENT_METHOD mappings of the four settlement event types are copied from
-- INSTALLMENT_PAYMENT_RECEIVED's, so they start on the same accounts and are then maintained like any
-- other mapping. The _REVERSED twins mirror. Never edit this file once applied.
-- ============================================================

CREATE TEMP TABLE v45_lines (event_type_code VARCHAR(60), line_no INT, direction_code VARCHAR(10),
                             deriv VARCHAR(20), account_code VARCHAR(30), field_code VARCHAR(60),
                             amount_field VARCHAR(60)) ON COMMIT DROP;
INSERT INTO v45_lines
SELECT t.code || CASE WHEN tw.twin THEN '_REVERSED' ELSE '' END, l.line_no,
       CASE WHEN tw.twin THEN CASE l.dir WHEN 'DEBIT' THEN 'CREDIT' ELSE 'DEBIT' END ELSE l.dir END,
       l.deriv, l.acct, l.fld, l.amt
  FROM (VALUES ('CONTRACT_SETTLEMENT'), ('COMPLAINT_SETTLEMENT')) t(code)
 CROSS JOIN (VALUES (FALSE), (TRUE)) tw(twin)
 CROSS JOIN (VALUES (1, 'DEBIT',  'MAPPING',  NULL::VARCHAR, 'PAYMENT_METHOD', 'paidAmount'),
                    (2, 'DEBIT',  'CONSTANT', '210201',      NULL,             'discountAmount'),
                    (3, 'CREDIT', 'CONSTANT', '110201',      NULL,             'totalAmount')) l(line_no, dir, deriv, acct, fld, amt);

DELETE FROM fin_rule_line_dim d
 USING fin_rule_line l, fin_event_type_rule r
 WHERE d.rule_line_id = l.rule_line_pk AND l.event_type_rule_id = r.event_type_rule_pk
   AND r.event_type_code IN (SELECT DISTINCT event_type_code FROM v45_lines);
DELETE FROM fin_rule_line l
 USING fin_event_type_rule r
 WHERE l.event_type_rule_id = r.event_type_rule_pk
   AND r.event_type_code IN (SELECT DISTINCT event_type_code FROM v45_lines);

INSERT INTO fin_rule_line (rule_line_pk, event_type_rule_id, line_no, account_derivation_type_code,
                           account_derivation_value, account_business_field_code, amount_source_type_code,
                           amount_source_value, direction_code, distribution_type_code, is_remainder_fl, created_at)
SELECT nextval('seq_fin_rule_line'), r.event_type_rule_pk, v.line_no, v.deriv, v.account_code, v.field_code,
       'FIELD', v.amount_field, v.direction_code, 'FIXED', FALSE, now()
  FROM v45_lines v JOIN fin_event_type_rule r ON r.event_type_code = v.event_type_code;

INSERT INTO fin_rule_line_dim (rule_line_dimension_pk, value_source_code, business_field_code, rule_line_id,
                               dimension_id, dimension_value_id, created_at)
SELECT nextval('seq_fin_rule_line_dim'), 'BUSINESS_FIELD', d.field, l.rule_line_pk, dm.dimension_pk, NULL, now()
  FROM fin_rule_line l
  JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
  JOIN (VALUES ('ORG', 'ORGANISATION_CODE'), ('BRANCH', 'BRANCH_CODE')) d(dim, field) ON TRUE
  JOIN fin_dimension dm ON dm.code = d.dim
 WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v45_lines);

-- PAYMENT_METHOD mappings for the four settlement types, copied from the installment payment's
INSERT INTO fin_account_mapping (account_mapping_pk, event_type_code, business_field_code, business_value,
                                 account_id, is_active_fl, created_by, created_at)
SELECT nextval('seq_fin_account_mapping'), t.code, m.business_field_code, m.business_value, m.account_id, TRUE, 'SYSTEM', now()
  FROM (SELECT DISTINCT event_type_code AS code FROM v45_lines) t
  JOIN fin_account_mapping m ON m.event_type_code = 'INSTALLMENT_PAYMENT_RECEIVED'
                            AND m.business_field_code = 'PAYMENT_METHOD' AND m.is_active_fl
 WHERE NOT EXISTS (SELECT 1 FROM fin_account_mapping x WHERE x.event_type_code = t.code
                     AND x.business_field_code = m.business_field_code AND x.business_value = m.business_value);

DO $$
DECLARE n INT;
BEGIN
  SELECT count(*) INTO n FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
   WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v45_lines);
  IF n <> 12 THEN RAISE EXCEPTION 'V45: expected 12 settlement rule lines (4 rules x 3), found %', n; END IF;
  SELECT count(*) INTO n FROM fin_account_mapping
   WHERE event_type_code IN (SELECT DISTINCT event_type_code FROM v45_lines)
     AND business_field_code = 'PAYMENT_METHOD' AND business_value IN ('CASH', 'CHEQUE') AND is_active_fl;
  IF n <> 8 THEN RAISE EXCEPTION 'V45: expected 8 PAYMENT_METHOD mappings (4 types x CASH/CHEQUE), found %', n; END IF;
END $$;
