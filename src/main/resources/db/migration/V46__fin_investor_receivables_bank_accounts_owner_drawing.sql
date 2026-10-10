-- ============================================================
-- V46 — FIN — investor-owned customer receivables, real bank accounts, owner drawings
-- ============================================================
-- Approved by the accountant 2026-10-10 (accountant notes, version 2):
--
-- 1. CUSTOMERS IN A BROKERAGE. The customer owes the INVESTOR, not the company. The customer balance
--    stays on the balance sheet, offset by a new liability 210203 "ذمم عملاء لحساب المستثمرين"
--    (net effect on the company: zero). Every collection is credited to the investors (210201).
--      INVESTOR_SALES_INVOICE   Dr 210201 investors       / Cr 4101 sales                   @amount
--      CONTRACT_CREATED         Dr 110201 customers        / Cr 210203 held for investors   @totalContract
--      INSTALLMENT_PAYMENT_RECEIVED, COURT_INSTALLMENT_PAYMENT
--                               Dr PAYMENT_METHOD / Cr 110201  +  Dr 210203 / Cr 210201  @amount
--      CONTRACT_SETTLEMENT, COMPLAINT_SETTLEMENT (compound)
--                               Dr PAYMENT_METHOD @paidAmount, Dr 210203 @totalAmount
--                               / Cr 110201 @totalAmount, Cr 210201 @paidAmount
--                               (the discount reduces what the investor receives — the investor bears it)
--    Before V46 the contract debited customers with the cash price only (990) while the customer
--    repays the contract total (1,536), so the customers account drifted to credit.
--
-- 2. BANK ACCOUNTS. LOAN_SYS records WHICH bank account a non-cash movement used. PKG_ACCOUNTING_EVENT
--    (19c/02-package-19c.sql, 2026-10-10) now sends PAYMENT_METHOD = 'BANK_<BANK_ACCOUNT_ID>' when a bank
--    account is chosen, 'CHEQUE' when none is (and 'CASH' as before). Accountant: Finance House
--    (بيت التمويل) has its own account, the commercial bank (البنك التجاري) its own, and a movement
--    with no bank goes to the commercial bank.
--      LOAN_SYS BANK_ACCOUNT  21 بيت التمويل الوسيط الذهبى → 110113   61 بيت التمويل افضل وسيط → 110113
--                             62 البنك التجارى الوسائط الفضية → 110111   81 "-" → 110111   CHEQUE → 110111
--    A new bank account in LOAN_SYS needs one mapping row per event type (FIN screen "account mappings");
--    until then its events are held by fin-consumer, never posted to a wrong account.
--    The safe transfers now take the bank side from the mapping too (they hard-coded 110111).
--
-- 3. OWNER_DRAWING (LOAN_PAYMENT FL 130, "دعم / مسحوبات صاحب الشركة"): Dr 110309 owner current /
--    Cr PAYMENT_METHOD; a negative value posts the _REVERSED twin (owner deposit). Legacy screen 38:
--    positive → Dr "جاري الشريك" / Cr cash.
--
-- The _REVERSED twins mirror every line. Each rewritten line carries the ORG / BRANCH dimensions.
-- October 2026 entries posted under the old rules are re-posted separately (reversal + replay), see
-- governance/project-artifacts/backend/oracle-cutover/19c/05-banks-investor-receivables-deploy.sql.
-- Never edit this file once applied.
-- ============================================================

-- ---- 1. lookups --------------------------------------------------------------------------------
INSERT INTO mdl_lookup_value (lookup_value_pk, lookup_type_id, code, name_ar, name_en, sort_order, is_active_fl, created_by, created_at)
SELECT nextval('seq_mdl_lookup_value'), t.lookup_type_pk, v.code, v.name_ar, v.name_en, v.sort_order, TRUE, 'SYSTEM', now()
  FROM (VALUES ('BANK_61', 'بنك بيت التمويل - أفضل وسيط',      'Finance House - Best Broker',   3),
               ('BANK_21', 'بنك بيت التمويل - الوسيط الذهبي',   'Finance House - Golden Broker', 4),
               ('BANK_62', 'البنك التجاري - الوسائط الفضية',     'Commercial Bank - Silver Media', 5),
               ('BANK_81', 'حساب بنكي غير مسمى (-)',             'Unnamed bank account (-)',       6)) v(code, name_ar, name_en, sort_order)
  JOIN mdl_lookup_type t ON t.key = 'PAYMENT_METHOD' AND t.owner_module_code = 'FIN'
ON CONFLICT (lookup_type_id, code) DO NOTHING;

INSERT INTO mdl_lookup_value (lookup_value_pk, lookup_type_id, code, name_ar, name_en, sort_order, is_active_fl, created_by, created_at)
SELECT nextval('seq_mdl_lookup_value'), t.lookup_type_pk, v.code, v.name_ar, v.name_en, v.sort_order, TRUE, 'SYSTEM', now()
  FROM (VALUES ('OWNER_DRAWING',          'دعم / مسحوبات صاحب الشركة',     'Owner drawing or support',             22),
               ('OWNER_DRAWING_REVERSED', 'عكس دعم / مسحوبات صاحب الشركة', 'Reversal of owner drawing or support', 122)) v(code, name_ar, name_en, sort_order)
  JOIN mdl_lookup_type t ON t.key = 'ACCOUNTING_EVENT_TYPE' AND t.owner_module_code = 'FIN'
ON CONFLICT (lookup_type_id, code) DO NOTHING;

-- ---- 2. account 210203 -------------------------------------------------------------------------
INSERT INTO fin_account (account_pk, code, name_ar, name_en, account_type_code, nature_code,
                         parent_account_id, is_leaf_fl, is_active_fl, created_by, created_at)
SELECT nextval('seq_fin_account'), '210203', 'ذمم عملاء لحساب المستثمرين', 'Customer receivables held for investors',
       'LIABILITY', 'CREDIT', p.account_pk, TRUE, TRUE, 'SYSTEM', now()
  FROM fin_account p
 WHERE p.code = '2102' AND NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = '210203');

-- ---- 3. rules ----------------------------------------------------------------------------------
INSERT INTO fin_event_type_rule (event_type_rule_pk, event_type_code, name_ar, name_en, is_active_fl, created_by, created_at)
SELECT nextval('seq_fin_event_type_rule'), v.code, v.name_ar, v.name_en, TRUE, 'SYSTEM', now()
  FROM (VALUES ('OWNER_DRAWING',          'دعم / مسحوبات صاحب الشركة',     'Owner drawing or support'),
               ('OWNER_DRAWING_REVERSED', 'عكس دعم / مسحوبات صاحب الشركة', 'Reversal of owner drawing or support')) v(code, name_ar, name_en)
ON CONFLICT (event_type_code) DO NOTHING;

CREATE TEMP TABLE v46_lines (event_type_code VARCHAR(60), line_no INT, direction_code VARCHAR(10),
                             deriv VARCHAR(20), account_code VARCHAR(30), field_code VARCHAR(60),
                             amount_field VARCHAR(60)) ON COMMIT DROP;
INSERT INTO v46_lines
SELECT t.code || CASE WHEN tw.twin THEN '_REVERSED' ELSE '' END, l.line_no,
       CASE WHEN tw.twin THEN CASE l.dir WHEN 'DEBIT' THEN 'CREDIT' ELSE 'DEBIT' END ELSE l.dir END,
       l.deriv, l.acct, l.fld, l.amt
  FROM (VALUES
    -- event type,                 line, dir,     derivation, account, field,            amount
    ('INVESTOR_SALES_INVOICE',       1, 'DEBIT',  'CONSTANT', '210201', NULL,             'amount'),
    ('INVESTOR_SALES_INVOICE',       2, 'CREDIT', 'CONSTANT', '4101',   NULL,             'amount'),
    ('CONTRACT_CREATED',             1, 'DEBIT',  'CONSTANT', '110201', NULL,             'totalContract'),
    ('CONTRACT_CREATED',             2, 'CREDIT', 'CONSTANT', '210203', NULL,             'totalContract'),
    ('INSTALLMENT_PAYMENT_RECEIVED', 1, 'DEBIT',  'MAPPING',  NULL,     'PAYMENT_METHOD', 'amount'),
    ('INSTALLMENT_PAYMENT_RECEIVED', 2, 'CREDIT', 'CONSTANT', '110201', NULL,             'amount'),
    ('INSTALLMENT_PAYMENT_RECEIVED', 3, 'DEBIT',  'CONSTANT', '210203', NULL,             'amount'),
    ('INSTALLMENT_PAYMENT_RECEIVED', 4, 'CREDIT', 'CONSTANT', '210201', NULL,             'amount'),
    ('COURT_INSTALLMENT_PAYMENT',    1, 'DEBIT',  'MAPPING',  NULL,     'PAYMENT_METHOD', 'amount'),
    ('COURT_INSTALLMENT_PAYMENT',    2, 'CREDIT', 'CONSTANT', '110201', NULL,             'amount'),
    ('COURT_INSTALLMENT_PAYMENT',    3, 'DEBIT',  'CONSTANT', '210203', NULL,             'amount'),
    ('COURT_INSTALLMENT_PAYMENT',    4, 'CREDIT', 'CONSTANT', '210201', NULL,             'amount'),
    ('CONTRACT_SETTLEMENT',          1, 'DEBIT',  'MAPPING',  NULL,     'PAYMENT_METHOD', 'paidAmount'),
    ('CONTRACT_SETTLEMENT',          2, 'CREDIT', 'CONSTANT', '110201', NULL,             'totalAmount'),
    ('CONTRACT_SETTLEMENT',          3, 'DEBIT',  'CONSTANT', '210203', NULL,             'totalAmount'),
    ('CONTRACT_SETTLEMENT',          4, 'CREDIT', 'CONSTANT', '210201', NULL,             'paidAmount'),
    ('COMPLAINT_SETTLEMENT',         1, 'DEBIT',  'MAPPING',  NULL,     'PAYMENT_METHOD', 'paidAmount'),
    ('COMPLAINT_SETTLEMENT',         2, 'CREDIT', 'CONSTANT', '110201', NULL,             'totalAmount'),
    ('COMPLAINT_SETTLEMENT',         3, 'DEBIT',  'CONSTANT', '210203', NULL,             'totalAmount'),
    ('COMPLAINT_SETTLEMENT',         4, 'CREDIT', 'CONSTANT', '210201', NULL,             'paidAmount'),
    ('SAFE_TRANSFER_TO_SAFE',        1, 'DEBIT',  'CONSTANT', '110101', NULL,             'amount'),
    ('SAFE_TRANSFER_TO_SAFE',        2, 'CREDIT', 'MAPPING',  NULL,     'PAYMENT_METHOD', 'amount'),
    ('SAFE_TRANSFER_TO_BANK',        1, 'DEBIT',  'MAPPING',  NULL,     'PAYMENT_METHOD', 'amount'),
    ('SAFE_TRANSFER_TO_BANK',        2, 'CREDIT', 'CONSTANT', '110101', NULL,             'amount'),
    ('OWNER_DRAWING',                1, 'DEBIT',  'CONSTANT', '110309', NULL,             'amount'),
    ('OWNER_DRAWING',                2, 'CREDIT', 'MAPPING',  NULL,     'PAYMENT_METHOD', 'amount')
  ) l(code, line_no, dir, deriv, acct, fld, amt)
  JOIN (SELECT DISTINCT code FROM (VALUES ('INVESTOR_SALES_INVOICE'), ('CONTRACT_CREATED'), ('INSTALLMENT_PAYMENT_RECEIVED'),
          ('COURT_INSTALLMENT_PAYMENT'), ('CONTRACT_SETTLEMENT'), ('COMPLAINT_SETTLEMENT'), ('SAFE_TRANSFER_TO_SAFE'),
          ('SAFE_TRANSFER_TO_BANK'), ('OWNER_DRAWING')) x(code)) t ON t.code = l.code
 CROSS JOIN (VALUES (FALSE), (TRUE)) tw(twin);

DELETE FROM fin_rule_line_dim d
 USING fin_rule_line l, fin_event_type_rule r
 WHERE d.rule_line_id = l.rule_line_pk AND l.event_type_rule_id = r.event_type_rule_pk
   AND r.event_type_code IN (SELECT DISTINCT event_type_code FROM v46_lines);
DELETE FROM fin_rule_line l
 USING fin_event_type_rule r
 WHERE l.event_type_rule_id = r.event_type_rule_pk
   AND r.event_type_code IN (SELECT DISTINCT event_type_code FROM v46_lines);

INSERT INTO fin_rule_line (rule_line_pk, event_type_rule_id, line_no, account_derivation_type_code,
                           account_derivation_value, account_business_field_code, amount_source_type_code,
                           amount_source_value, direction_code, distribution_type_code, is_remainder_fl, created_at)
SELECT nextval('seq_fin_rule_line'), r.event_type_rule_pk, v.line_no, v.deriv, v.account_code, v.field_code,
       'FIELD', v.amount_field, v.direction_code, 'FIXED', FALSE, now()
  FROM v46_lines v JOIN fin_event_type_rule r ON r.event_type_code = v.event_type_code;

INSERT INTO fin_rule_line_dim (rule_line_dimension_pk, value_source_code, business_field_code, rule_line_id,
                               dimension_id, dimension_value_id, created_at)
SELECT nextval('seq_fin_rule_line_dim'), 'BUSINESS_FIELD', d.field, l.rule_line_pk, dm.dimension_pk, NULL, now()
  FROM fin_rule_line l
  JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
  JOIN (VALUES ('ORG', 'ORGANISATION_CODE'), ('BRANCH', 'BRANCH_CODE')) d(dim, field) ON TRUE
  JOIN fin_dimension dm ON dm.code = d.dim
 WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v46_lines);

-- ---- 4. PAYMENT_METHOD mappings: every event type whose rule maps PAYMENT_METHOD -------------
CREATE TEMP TABLE v46_pm (business_value VARCHAR(30), account_code VARCHAR(30)) ON COMMIT DROP;
INSERT INTO v46_pm VALUES ('CASH', '110101'), ('CHEQUE', '110111'),
                          ('BANK_21', '110113'), ('BANK_61', '110113'), ('BANK_62', '110111'), ('BANK_81', '110111');

INSERT INTO fin_account_mapping (account_mapping_pk, event_type_code, business_field_code, business_value,
                                 account_id, is_active_fl, created_by, created_at)
SELECT nextval('seq_fin_account_mapping'), t.event_type_code, 'PAYMENT_METHOD', p.business_value, a.account_pk, TRUE, 'SYSTEM', now()
  FROM (SELECT DISTINCT r.event_type_code
          FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
         WHERE l.account_derivation_type_code = 'MAPPING' AND l.account_business_field_code = 'PAYMENT_METHOD') t
 CROSS JOIN v46_pm p
  JOIN fin_account a ON a.code = p.account_code
 WHERE NOT EXISTS (SELECT 1 FROM fin_account_mapping x WHERE x.event_type_code = t.event_type_code
                     AND x.business_field_code = 'PAYMENT_METHOD' AND x.business_value = p.business_value);

-- ---- 5. verification ---------------------------------------------------------------------------
DO $$
DECLARE n INT;
BEGIN
  SELECT count(*) INTO n FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
   WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v46_lines);
  IF n <> 52 THEN RAISE EXCEPTION 'V46: expected 52 rule lines (26 x 2 twins), found %', n; END IF;
  -- every rule that maps PAYMENT_METHOD resolves all six values
  SELECT count(*) INTO n
    FROM (SELECT DISTINCT r.event_type_code
            FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
           WHERE l.account_derivation_type_code = 'MAPPING' AND l.account_business_field_code = 'PAYMENT_METHOD') t
   CROSS JOIN v46_pm p
   WHERE NOT EXISTS (SELECT 1 FROM fin_account_mapping m WHERE m.event_type_code = t.event_type_code
                       AND m.business_field_code = 'PAYMENT_METHOD' AND m.business_value = p.business_value AND m.is_active_fl);
  IF n <> 0 THEN RAISE EXCEPTION 'V46: % PAYMENT_METHOD mappings missing', n; END IF;
  -- every rule balances: the debit and credit amount fields are the same multiset
  SELECT count(*) INTO n FROM (
    SELECT r.event_type_code
      FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
     WHERE r.event_type_code IN (SELECT DISTINCT event_type_code FROM v46_lines)
     GROUP BY r.event_type_code
    HAVING string_agg(CASE WHEN l.direction_code = 'DEBIT'  THEN l.amount_source_value END, ',' ORDER BY l.amount_source_value)
        IS DISTINCT FROM
           string_agg(CASE WHEN l.direction_code = 'CREDIT' THEN l.amount_source_value END, ',' ORDER BY l.amount_source_value)) x;
  IF n <> 0 THEN RAISE EXCEPTION 'V46: % rules whose debit and credit amount fields differ', n; END IF;
END $$;
