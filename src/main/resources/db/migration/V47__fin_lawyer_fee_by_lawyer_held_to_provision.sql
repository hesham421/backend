-- ============================================================
-- V47 — FIN — lawyer-fee payments credited by lawyer: the held lawyer's fees go to the provision
-- ============================================================
-- Accountant (المشاكل والحلول, 2026-10-10): of the lawyers, two are paid every month (expenses) and one
-- has his fees held — "an amount owed to him by the company, to be shown as a provision" — and asked
-- whether that lawyer's fees can be linked to the provision directly. They can: LOAN_SYS knows the
-- lawyer of every complaint (COMPLAINTS.LAWYER_NAME_FK, names in SYSTEM_TANSACTION_DT group 15).
-- 2026 measured: fees collected / paid out — 138 مزيد المطيري 17,548 / 16,910 · 145 سارة أحمد 5,463 /
-- 5,404 · 40 وليد حسن 2,347 / 2,715 · 41 خالد الظفيري 6,388 / 0  → the held lawyer is 41.
--
-- PKG_ACCOUNTING_EVENT (v6) sends fields.LAWYER_CODE on LAWYER_FEE_PAYMENT ('NONE' when unknown).
--   LAWYER_FEE_PAYMENT   Dr PAYMENT_METHOD / Cr MAPPING(LAWYER_CODE):  41 → 211001 مخصص محاماة,
--                        138, 145, 40, NONE → 4403 إيرادات استرداد أتعاب قضائية (as before)
-- A new lawyer in LOAN_SYS needs one mapping row (FIN screen "account mappings"); until then the
-- event is held by fin-consumer, never posted to a wrong account. Applied directly on production
-- (2026-10-10), not through Flyway. Never edit this file once applied.
-- ============================================================

INSERT INTO mdl_lookup_value (lookup_value_pk, lookup_type_id, code, name_ar, name_en, sort_order, is_active_fl, created_by, created_at)
SELECT nextval('seq_mdl_lookup_value'), t.lookup_type_pk, 'LAWYER_CODE', 'رمز المحامي', 'Lawyer code', 5, TRUE, 'SYSTEM', now()
  FROM mdl_lookup_type t WHERE t.key = 'FIN_EVENT_BUSINESS_FIELD' AND t.owner_module_code = 'FIN'
ON CONFLICT (lookup_type_id, code) DO NOTHING;

UPDATE fin_rule_line l
   SET account_derivation_type_code = 'MAPPING', account_derivation_value = NULL, account_business_field_code = 'LAWYER_CODE'
  FROM fin_event_type_rule r
 WHERE l.event_type_rule_id = r.event_type_rule_pk
   AND r.event_type_code IN ('LAWYER_FEE_PAYMENT', 'LAWYER_FEE_PAYMENT_REVERSED')
   AND l.account_derivation_type_code = 'CONSTANT' AND l.account_derivation_value = '4403';

INSERT INTO fin_account_mapping (account_mapping_pk, event_type_code, business_field_code, business_value,
                                 account_id, is_active_fl, created_by, created_at)
SELECT nextval('seq_fin_account_mapping'), t.code, 'LAWYER_CODE', m.lawyer, a.account_pk, TRUE, 'SYSTEM', now()
  FROM (VALUES ('LAWYER_FEE_PAYMENT'), ('LAWYER_FEE_PAYMENT_REVERSED')) t(code)
 CROSS JOIN (VALUES ('41', '211001'), ('138', '4403'), ('145', '4403'), ('40', '4403'), ('NONE', '4403')) m(lawyer, acct)
  JOIN fin_account a ON a.code = m.acct
 WHERE NOT EXISTS (SELECT 1 FROM fin_account_mapping x WHERE x.event_type_code = t.code
                     AND x.business_field_code = 'LAWYER_CODE' AND x.business_value = m.lawyer);

DO $$
DECLARE n INT;
BEGIN
  SELECT count(*) INTO n FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
   WHERE r.event_type_code IN ('LAWYER_FEE_PAYMENT', 'LAWYER_FEE_PAYMENT_REVERSED') AND l.account_business_field_code = 'LAWYER_CODE';
  IF n <> 2 THEN RAISE EXCEPTION 'V47: expected 2 LAWYER_CODE rule lines, found %', n; END IF;
  SELECT count(*) INTO n FROM fin_account_mapping WHERE business_field_code = 'LAWYER_CODE' AND is_active_fl;
  IF n <> 10 THEN RAISE EXCEPTION 'V47: expected 10 LAWYER_CODE mappings, found %', n; END IF;
END $$;
