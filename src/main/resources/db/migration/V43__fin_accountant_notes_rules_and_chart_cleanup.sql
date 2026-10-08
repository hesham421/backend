-- ============================================================
-- V43 — FIN — accountant's notes (2026-10-08): settlement rules, new items, chart cleanup
-- ============================================================
-- Source: the accountant's sheet «ملاحظات البرنامج.xlsx» and the opening-balance workbook
-- «الارصدة الافتتاحية2.xlsx» (trial-balance and balance-sheet layout), confirmed by the owner:
--
-- 1. CUSTOMER SETTLEMENTS — a compound entry: Dr bank/cash (what was collected) + Dr shareholders'
--    current account (the discount granted) / Cr customers (the total). E.g. a debt of 100 settled
--    with 80 cash and 20 discount: Dr cash 80, Dr shareholders 20, Cr customers 100.
--    The legacy system records the two halves as two separate rows, so FIN posts them as two
--    entries that together ARE that compound entry:
--      * the cash half — INSTALLMENT_PAYMENT_RECEIVED / COURT_INSTALLMENT_PAYMENT, unchanged:
--        Dr bank|cash / Cr customers;
--      * the discount half — CONTRACT_SETTLEMENT and COMPLAINT_SETTLEMENT now debit the
--        shareholders' current account (210201) instead of 4901 / 6602. Their _REVERSED twins mirror.
--    Customers stay an ordinary balance-sheet asset (owner's answer).
-- 2. NEW ITEMS the sheet asks for, under the groups it names:
--    other debit balances — owner's current/drawings, shareholders' loan, custody shortage
--    (embezzlement), employee advances; other credit balances — lawyer provision, customer-refund
--    provision, court-fees provision, credit balances (shareholders); revenue — complaint/service
--    revenue, bank profits; expenses — general sundry, operating salaries, operating rent, operating
--    professional fees, other expenses; plus an equity line for the opening reconciliation
--    difference («تسوية فروقات / عجز» in the opening workbook).
-- 3. CLEANUP — every account that is neither in the list below nor referenced by anything (a rule,
--    a mapping, a journal line, a template, an allocation, the retained-earnings flag) is deleted.
-- Reuse is by CURRENT code (the V40 codes); a reused account keeps its pk, so mappings follow it.
-- Never edit this file once applied.
-- ============================================================

CREATE TEMP TABLE coa_keep (
  code         VARCHAR(30) PRIMARY KEY,
  parent_code  VARCHAR(30),
  type_code    VARCHAR(20) NOT NULL,
  nature_code  VARCHAR(10) NOT NULL,
  is_leaf_fl   BOOLEAN     NOT NULL,
  name_ar      VARCHAR(200) NOT NULL,
  name_en      VARCHAR(200) NOT NULL,
  from_code    VARCHAR(30)            -- an existing account moved to this code (pk kept)
) ON COMMIT DROP;

INSERT INTO coa_keep VALUES
-- 1 ASSETS
('1',      NULL,   'ASSET','DEBIT', FALSE,'الأصول','Assets',NULL),
('11',     '1',    'ASSET','DEBIT', FALSE,'الأصول المتداولة','Current assets',NULL),
('1101',   '11',   'ASSET','DEBIT', FALSE,'النقد وما في حكمه','Cash and cash equivalents',NULL),
('110101', '1101', 'ASSET','DEBIT', TRUE, 'الصندوق','Cash box',NULL),
('110103', '1101', 'ASSET','DEBIT', TRUE, 'نقد بالطريق وتحويلات بنكية','Cash in transit and bank transfers',NULL),
('110111', '1101', 'ASSET','DEBIT', TRUE, 'البنك التجاري','Commercial Bank',NULL),
('110113', '1101', 'ASSET','DEBIT', TRUE, 'بنك بيت التمويل','Finance House bank',NULL),
('1102',   '11',   'ASSET','DEBIT', FALSE,'العملاء','Customers',NULL),
('110201', '1102', 'ASSET','DEBIT', TRUE, 'العملاء','Customers',NULL),
('1103',   '11',   'ASSET','DEBIT', FALSE,'أرصدة مدينة أخرى','Other debit balances',NULL),
('110303', '1103', 'ASSET','DEBIT', TRUE, 'عهد المحاماة والمحكمة','Lawyer and court custody advances',NULL),
('110304', '1103', 'ASSET','DEBIT', TRUE, 'سلف الموظفين','Employee advances',NULL),
('110307', '1103', 'ASSET','DEBIT', TRUE, 'قرض المساهمين','Shareholders loan',NULL),
('110309', '1103', 'ASSET','DEBIT', TRUE, 'جاري المالك / مسحوبات المالك','Owner current account / drawings','340101'),
('110310', '1103', 'ASSET','DEBIT', TRUE, 'عجز عهد (اختلاس)','Custody shortage (embezzlement)',NULL),
('1104',   '11',   'ASSET','DEBIT', FALSE,'المخزون','Inventory',NULL),
('110401', '1104', 'ASSET','DEBIT', TRUE, 'المخزون','Inventory',NULL),
('12',     '1',    'ASSET','DEBIT', FALSE,'الأصول غير المتداولة','Non-current assets',NULL),
('1202',   '12',   'ASSET','DEBIT', FALSE,'الأصول الثابتة','Property and equipment',NULL),
('120203', '1202', 'ASSET','DEBIT', TRUE, 'سيارات','Vehicles',NULL),
('120204', '1202', 'ASSET','DEBIT', TRUE, 'أثاث','Furniture',NULL),
('120205', '1202', 'ASSET','DEBIT', TRUE, 'أجهزة','Equipment and computers',NULL),
('1203',   '12',   'ASSET','DEBIT', FALSE,'(-) مجمع الإهلاك','Less: accumulated depreciation',NULL),
('120303', '1203', 'ASSET','DEBIT', TRUE, 'مجمع إهلاك سيارات','Accumulated depreciation - vehicles',NULL),
('120304', '1203', 'ASSET','DEBIT', TRUE, 'مجمع إهلاك أثاث','Accumulated depreciation - furniture',NULL),
('120305', '1203', 'ASSET','DEBIT', TRUE, 'مجمع إهلاك أجهزة','Accumulated depreciation - equipment',NULL),
-- 2 LIABILITIES
('2',      NULL,   'LIABILITY','CREDIT',FALSE,'الخصوم','Liabilities',NULL),
('21',     '2',    'LIABILITY','CREDIT',FALSE,'الخصوم المتداولة','Current liabilities',NULL),
('2101',   '21',   'LIABILITY','CREDIT',FALSE,'الموردون','Suppliers',NULL),
('210101', '2101', 'LIABILITY','CREDIT',TRUE, 'الموردون','Suppliers',NULL),
('2102',   '21',   'LIABILITY','CREDIT',FALSE,'جاري المساهمين','Shareholders current accounts',NULL),
('210201', '2102', 'LIABILITY','CREDIT',TRUE, 'جاري المساهمين','Shareholders current accounts',NULL),
('2110',   '21',   'LIABILITY','CREDIT',FALSE,'أرصدة دائنة أخرى','Other credit balances',NULL),
('211001', '2110', 'LIABILITY','CREDIT',TRUE, 'مخصص محاماة','Lawyer fees provision',NULL),
('211002', '2110', 'LIABILITY','CREDIT',TRUE, 'مخصص استرداد عملاء','Customer refunds provision',NULL),
('211003', '2110', 'LIABILITY','CREDIT',TRUE, 'مخصص رسوم محكمة','Court fees provision',NULL),
('211004', '2110', 'LIABILITY','CREDIT',TRUE, 'أرصدة دائنة (مساهمين)','Credit balances (shareholders)',NULL),
-- 3 EQUITY
('3',      NULL,   'EQUITY','CREDIT',FALSE,'حقوق الملكية','Equity',NULL),
('3101',   '3',    'EQUITY','CREDIT',FALSE,'رأس المال','Share capital',NULL),
('310101', '3101', 'EQUITY','CREDIT',TRUE, 'رأس المال','Share capital',NULL),
('3301',   '3',    'EQUITY','CREDIT',FALSE,'الأرباح المرحلة','Retained earnings',NULL),
('330101', '3301', 'EQUITY','CREDIT',TRUE, 'أرباح مرحلة','Retained earnings brought forward',NULL),
('3501',   '3',    'EQUITY','CREDIT',TRUE, 'تسوية فروقات / عجز','Reconciliation differences / shortage',NULL),
-- 4 REVENUE
('4',      NULL,   'REVENUE','CREDIT',FALSE,'الإيرادات','Revenue',NULL),
('41',     '4',    'REVENUE','CREDIT',FALSE,'المبيعات','Sales',NULL),
('4101',   '41',   'REVENUE','CREDIT',TRUE, 'المبيعات','Sales',NULL),
('44',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات الشكاوى والخدمات','Complaint and service income',NULL),
('4401',   '44',   'REVENUE','CREDIT',TRUE, 'إيرادات شكوى / خدمات','Complaint / service income',NULL),
('4403',   '44',   'REVENUE','CREDIT',TRUE, 'إيرادات استرداد أتعاب قضائية','Recovered legal fees',NULL),
('45',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات أخرى','Other income',NULL),
('4501',   '45',   'REVENUE','CREDIT',TRUE, 'إيرادات أخرى','Other income',NULL),
('4504',   '45',   'REVENUE','CREDIT',TRUE, 'إيرادات أرباح البنوك','Bank profit income',NULL),
-- 5 COST OF REVENUE (periodic inventory: opening stock + purchases - closing stock)
('5',      NULL,   'EXPENSE','DEBIT', FALSE,'تكلفة المبيعات','Cost of sales',NULL),
('51',     '5',    'EXPENSE','DEBIT', FALSE,'المشتريات','Purchases',NULL),
('5102',   '51',   'EXPENSE','DEBIT', TRUE, 'المشتريات','Purchases',NULL),
-- 6 EXPENSES
('6',      NULL,   'EXPENSE','DEBIT', FALSE,'المصروفات','Expenses',NULL),
('61',     '6',    'EXPENSE','DEBIT', FALSE,'الرواتب','Salaries',NULL),
('6101',   '61',   'EXPENSE','DEBIT', TRUE, 'تشغيل / رواتب','Operating - salaries',NULL),
('62',     '6',    'EXPENSE','DEBIT', FALSE,'الإيجارات','Rent',NULL),
('6201',   '62',   'EXPENSE','DEBIT', TRUE, 'تشغيل / إيجار','Operating - rent',NULL),
('64',     '6',    'EXPENSE','DEBIT', FALSE,'الإهلاك','Depreciation',NULL),
('6401',   '64',   'EXPENSE','DEBIT', TRUE, 'مصروف الإهلاك','Depreciation expense',NULL),
('68',     '6',    'EXPENSE','DEBIT', FALSE,'مصروفات عمومية وأخرى','General and other expenses',NULL),
('6801',   '68',   'EXPENSE','DEBIT', TRUE, 'مصروفات عمومية متنوعة','Sundry general expenses',NULL),
('6806',   '68',   'EXPENSE','DEBIT', TRUE, 'تشغيل / أتعاب وإستشارات مهنية','Operating - professional fees',NULL),
('6899',   '68',   'EXPENSE','DEBIT', TRUE, 'مصروفات أخرى','Other expenses',NULL);

-- ----------------------------------------------------------------------------
-- 1. rules — the discount half of a settlement goes to the shareholders' current account
-- ----------------------------------------------------------------------------
UPDATE fin_rule_line l
   SET account_derivation_value = '210201'
  FROM fin_event_type_rule r
 WHERE r.event_type_rule_pk = l.event_type_rule_id
   AND r.event_type_code IN ('CONTRACT_SETTLEMENT', 'CONTRACT_SETTLEMENT_REVERSED',
                             'COMPLAINT_SETTLEMENT', 'COMPLAINT_SETTLEMENT_REVERSED')
   AND l.account_derivation_type_code = 'CONSTANT'
   AND l.account_derivation_value IN ('4901', '6602');

-- revenue type 181 (customer settlement income) joins type 1 on «إيرادات شكوى / خدمات» (4401)
UPDATE fin_account_mapping m
   SET account_id = (SELECT account_pk FROM fin_account WHERE code = '4401'), updated_by = 'SYSTEM', updated_at = now()
 WHERE m.account_id = (SELECT account_pk FROM fin_account WHERE code = '4402');

-- ----------------------------------------------------------------------------
-- 2. accounts — move, rewrite, insert, link
-- ----------------------------------------------------------------------------
UPDATE fin_account a SET code = k.code
  FROM coa_keep k WHERE k.from_code IS NOT NULL AND a.code = k.from_code
   AND NOT EXISTS (SELECT 1 FROM fin_account x WHERE x.code = k.code);

UPDATE fin_account a
   SET name_ar = k.name_ar, name_en = k.name_en, account_type_code = k.type_code, nature_code = k.nature_code,
       is_leaf_fl = k.is_leaf_fl, is_active_fl = TRUE, updated_by = 'SYSTEM', updated_at = now()
  FROM coa_keep k WHERE a.code = k.code;

INSERT INTO fin_account (account_pk, code, name_ar, name_en, account_type_code, nature_code,
                         parent_account_id, is_leaf_fl, is_active_fl, created_by, created_at)
SELECT nextval('seq_fin_account'), k.code, k.name_ar, k.name_en, k.type_code, k.nature_code, NULL, k.is_leaf_fl, TRUE, 'SYSTEM', now()
  FROM coa_keep k WHERE NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = k.code);

UPDATE fin_account a SET parent_account_id = p.account_pk
  FROM coa_keep k LEFT JOIN fin_account p ON p.code = k.parent_code
 WHERE a.code = k.code AND a.parent_account_id IS DISTINCT FROM p.account_pk;

-- ----------------------------------------------------------------------------
-- 3. cleanup — delete every account outside the list that nothing references (leaves first)
-- ----------------------------------------------------------------------------
CREATE TEMP TABLE coa_ref ON COMMIT DROP AS
SELECT account_id AS account_pk FROM fin_journal_line
UNION SELECT account_id FROM fin_account_mapping
UNION SELECT account_id FROM fin_recurring_template_line
UNION SELECT source_account_id FROM fin_allocation_rule
UNION SELECT target_account_id FROM fin_allocation_target
UNION SELECT a.account_pk FROM fin_rule_line l JOIN fin_account a ON a.code = l.account_derivation_value
       WHERE l.account_derivation_type_code = 'CONSTANT'
UNION SELECT account_pk FROM fin_account WHERE is_retained_earnings_fl;

DO $$
DECLARE n INT; total INT := 0; v_left TEXT;
BEGIN
  LOOP
    DELETE FROM fin_account a
     WHERE NOT EXISTS (SELECT 1 FROM coa_keep k WHERE k.code = a.code)
       AND NOT EXISTS (SELECT 1 FROM coa_ref r WHERE r.account_pk = a.account_pk)
       AND NOT EXISTS (SELECT 1 FROM fin_account c WHERE c.parent_account_id = a.account_pk);
    GET DIAGNOSTICS n = ROW_COUNT;
    total := total + n;
    EXIT WHEN n = 0;
  END LOOP;
  SELECT string_agg(code || ' ' || name_ar, ', ') INTO v_left FROM fin_account a
   WHERE NOT EXISTS (SELECT 1 FROM coa_keep k WHERE k.code = a.code);
  RAISE NOTICE 'V43: % unused accounts deleted; kept outside the list because still referenced: %', total, COALESCE(v_left, 'none');
END $$;

-- ----------------------------------------------------------------------------
-- 4. verification
-- ----------------------------------------------------------------------------
DO $$
DECLARE v_list TEXT; v_count INT;
BEGIN
  SELECT count(*) INTO v_count FROM coa_keep k WHERE NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = k.code);
  IF v_count > 0 THEN RAISE EXCEPTION 'V43: % listed accounts missing', v_count; END IF;

  SELECT string_agg(a.code, ', ') INTO v_list FROM fin_account a JOIN coa_keep k ON k.code = a.code
   WHERE a.parent_account_id IS DISTINCT FROM (SELECT p.account_pk FROM fin_account p WHERE p.code = k.parent_code);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: wrong parent: %', v_list; END IF;

  SELECT string_agg(a.code, ', ') INTO v_list FROM fin_account a
   WHERE a.is_leaf_fl AND EXISTS (SELECT 1 FROM fin_account c WHERE c.parent_account_id = a.account_pk);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: leaf accounts with children: %', v_list; END IF;

  SELECT string_agg(c.code, ', ') INTO v_list FROM fin_account c JOIN fin_account p ON p.account_pk = c.parent_account_id
   WHERE c.account_type_code <> p.account_type_code;
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: child/parent type mismatch: %', v_list; END IF;

  SELECT string_agg(code, ', ') INTO v_list FROM fin_account
   WHERE (account_type_code IN ('ASSET', 'EXPENSE') AND nature_code <> 'DEBIT')
      OR (account_type_code IN ('LIABILITY', 'EQUITY', 'REVENUE') AND nature_code <> 'CREDIT');
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: nature differs from class (breaks statement signs, see V41): %', v_list; END IF;

  SELECT count(*) INTO v_count FROM fin_account WHERE is_retained_earnings_fl AND code = '330101' AND is_leaf_fl AND is_active_fl;
  IF v_count <> 1 THEN RAISE EXCEPTION 'V43: retained earnings is not the active leaf 330101'; END IF;

  SELECT string_agg(r.event_type_code || ':' || l.line_no || '=' || COALESCE(l.account_derivation_value, 'NULL'), ', ') INTO v_list
    FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
   WHERE r.is_active_fl AND l.account_derivation_type_code = 'CONSTANT'
     AND NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = l.account_derivation_value AND a.is_leaf_fl AND a.is_active_fl);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: active rule lines on a missing / non-leaf / inactive account: %', v_list; END IF;

  SELECT string_agg(m.event_type_code || '/' || m.business_value, ', ') INTO v_list
    FROM fin_account_mapping m JOIN fin_account a ON a.account_pk = m.account_id
   WHERE m.is_active_fl AND NOT (a.is_leaf_fl AND a.is_active_fl);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: active mappings on a non-leaf / inactive account: %', v_list; END IF;

  -- settlements: the discount half debits the shareholders' account, the twin credits it
  SELECT string_agg(r.event_type_code, ', ') INTO v_list
    FROM fin_event_type_rule r
   WHERE r.event_type_code IN ('CONTRACT_SETTLEMENT', 'COMPLAINT_SETTLEMENT')
     AND NOT EXISTS (SELECT 1 FROM fin_rule_line l WHERE l.event_type_rule_id = r.event_type_rule_pk
                       AND l.direction_code = 'DEBIT' AND l.account_derivation_value = '210201');
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: settlement rules not debiting 210201: %', v_list; END IF;

  RAISE NOTICE 'V43: chart now % accounts (% postable leaves)',
    (SELECT count(*) FROM fin_account), (SELECT count(*) FROM fin_account WHERE is_leaf_fl AND is_active_fl);
END $$;
