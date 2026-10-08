-- ============================================================================
-- golive-2026-10-01-production.sql — FIN go-live on the LIVE Postgres, in ONE transaction
-- ============================================================================
-- Prepared 2026-10-08. Runs Flyway V40..V43 (exact files of src/main/resources/db/migration, comments
-- stripped) and records each in flyway_schema_history with Flyway's own checksum, so the next deploy
-- sees them as applied and never re-runs them; then posts the opening balances (DRAFT numbers — the
-- reconciliation line «تسوية فروقات / عجز» absorbs each branch's cash balance, which the workbook's
-- totals left out; the accountant corrects with ordinary manual entries from the FIN screen).
-- Refuses to run unless the database is exactly at V39. Any error rolls EVERYTHING back.
-- RUN ON THE SERVER:
--   docker exec -i <postgres-container> psql -U postgres -d erp_db -v ON_ERROR_STOP=1 < golive-2026-10-01-production.sql
-- ============================================================================
\set ON_ERROR_STOP on
BEGIN;
DO $$ BEGIN
  IF (SELECT max(version::int) FROM flyway_schema_history WHERE success) <> 39 THEN
    RAISE EXCEPTION 'golive: expected the database at Flyway V39, found V%', (SELECT max(version::int) FROM flyway_schema_history WHERE success);
  END IF;
END $$;
-- ===== V40
CREATE TEMP TABLE seed_coa (
  code         VARCHAR(30)  PRIMARY KEY,
  parent_code  VARCHAR(30),
  type_code    VARCHAR(20)  NOT NULL,
  nature_code  VARCHAR(10)  NOT NULL,
  is_leaf_fl   BOOLEAN      NOT NULL,
  name_ar      VARCHAR(200) NOT NULL,
  name_en      VARCHAR(200) NOT NULL,
  old_code     VARCHAR(30)
) ON COMMIT DROP;
INSERT INTO seed_coa (code, parent_code, type_code, nature_code, is_leaf_fl, name_ar, name_en, old_code) VALUES
('1',      NULL,   'ASSET','DEBIT', FALSE,'الأصول','Assets','0001'),
('11',     '1',    'ASSET','DEBIT', FALSE,'الأصول المتداولة','Current assets','00010002'),
('1101',   '11',   'ASSET','DEBIT', FALSE,'النقد وما في حكمه','Cash and cash equivalents','000100020001'),
('110101', '1101', 'ASSET','DEBIT', TRUE, 'الصندوق الرئيسي','Main cash box','0001000200010001'),
('110102', '1101', 'ASSET','DEBIT', TRUE, 'صناديق الفروع','Branch cash boxes','0001000200010002'),
('110103', '1101', 'ASSET','DEBIT', TRUE, 'نقد بالطريق وتحويلات بنكية','Cash in transit and bank transfers','0001000200010003'),
('110111', '1101', 'ASSET','DEBIT', TRUE, 'البنك التجاري','Commercial Bank','0001000200010007'),
('110112', '1101', 'ASSET','DEBIT', TRUE, 'مصرف الراجحي','Al Rajhi Bank','0001000200010005'),
('110113', '1101', 'ASSET','DEBIT', TRUE, 'بيت التمويل','Finance House bank','0001000200010006'),
('110119', '1101', 'ASSET','DEBIT', TRUE, 'بنوك أخرى','Other banks','0001000200010004'),
('1102',   '11',   'ASSET','DEBIT', FALSE,'ذمم التقسيط المدينة','Instalment receivables','000100020003'),
('110201', '1102', 'ASSET','DEBIT', TRUE, 'ذمم عملاء التقسيط - منتظمة','Instalment customers - performing','0001000200030001'),
('110202', '1102', 'ASSET','DEBIT', TRUE, 'ذمم عملاء التقسيط - متعثرة وتحت الإجراء القضائي','Instalment customers - defaulted / in litigation',NULL),
('110203', '1102', 'ASSET','CREDIT',TRUE, '(-) إيرادات تقسيط مؤجلة','Less: deferred instalment income','000200010099'),
('110204', '1102', 'ASSET','CREDIT',TRUE, '(-) مخصص الخسائر الائتمانية المتوقعة','Less: expected credit loss allowance','0002000100150015'),
('110205', '1102', 'ASSET','DEBIT', TRUE, 'شيكات أقساط مؤجلة الدفع','Post-dated instalment cheques',NULL),
('110206', '1102', 'ASSET','DEBIT', TRUE, 'شيكات تحت التحصيل','Cheques under collection','0001000200090001'),
('1103',   '11',   'ASSET','DEBIT', FALSE,'ذمم مدينة أخرى','Other receivables','000100020014'),
('110301', '1103', 'ASSET','DEBIT', TRUE, 'مصاريف قضائية مستردة من العملاء','Legal costs recoverable from customers',NULL),
('110302', '1103', 'ASSET','DEBIT', TRUE, 'مستحقات لدى المحكمة وإدارة التنفيذ','Amounts held by court and enforcement',NULL),
('110303', '1103', 'ASSET','DEBIT', TRUE, 'عهد المحاماة والمحكمة','Lawyer and court custody advances','0001000200060001'),
('110304', '1103', 'ASSET','DEBIT', TRUE, 'سلف العاملين','Employee advances','0001000200050001'),
('110305', '1103', 'ASSET','DEBIT', TRUE, 'عهد مؤقتة للموظفين','Temporary employee custody',NULL),
('110306', '1103', 'ASSET','DEBIT', TRUE, 'عهد مستديمة','Permanent custody (imprest)','0001000200060004'),
('110307', '1103', 'ASSET','DEBIT', TRUE, 'قروض ممنوحة للمستثمرين','Loans granted to investors','0001000200140001'),
('110308', '1103', 'ASSET','DEBIT', TRUE, 'أرصدة مدينة أخرى','Other debit balances',NULL),
('1104',   '11',   'ASSET','DEBIT', FALSE,'المخزون','Inventory','000100020002'),
('110401', '1104', 'ASSET','DEBIT', TRUE, 'بضاعة بالمخازن','Goods in stock','0001000200020001'),
('110402', '1104', 'ASSET','DEBIT', TRUE, 'بضاعة بالطريق','Goods in transit','0001000200020008'),
('1105',   '11',   'ASSET','DEBIT', FALSE,'مدفوعات مقدمة','Prepayments','000100020010'),
('110501', '1105', 'ASSET','DEBIT', TRUE, 'إيجارات مدفوعة مقدما','Prepaid rent','0001000200100001'),
('110502', '1105', 'ASSET','DEBIT', TRUE, 'مصروفات مدفوعة مقدما أخرى','Other prepaid expenses','0001000200100003'),
('110503', '1105', 'ASSET','DEBIT', TRUE, 'دفعات مقدمة للموردين','Advances to suppliers',NULL),
('12',     '1',    'ASSET','DEBIT', FALSE,'الأصول غير المتداولة','Non-current assets','00010001'),
('1201',   '12',   'ASSET','DEBIT', TRUE, 'ذمم تقسيط طويلة الأجل','Non-current instalment receivables',NULL),
('1202',   '12',   'ASSET','DEBIT', FALSE,'الممتلكات والمعدات','Property and equipment','000100010001'),
('120201', '1202', 'ASSET','DEBIT', TRUE, 'أراضي','Land','0001000100010001'),
('120202', '1202', 'ASSET','DEBIT', TRUE, 'مباني','Buildings','0001000100010002'),
('120203', '1202', 'ASSET','DEBIT', TRUE, 'وسائل نقل','Vehicles','0001000100010004'),
('120204', '1202', 'ASSET','DEBIT', TRUE, 'أثاث ومعدات مكتبية','Office furniture and equipment','0001000100010008'),
('120205', '1202', 'ASSET','DEBIT', TRUE, 'أجهزة حاسب آلي وطابعات','Computers and printers','0001000100010011'),
('1203',   '12',   'ASSET','CREDIT',FALSE,'(-) مجمع إهلاك الممتلكات والمعدات','Less: accumulated depreciation',NULL),
('120302', '1203', 'ASSET','CREDIT',TRUE, 'مجمع إهلاك مباني','Accumulated depreciation - buildings','0002000100150004'),
('120303', '1203', 'ASSET','CREDIT',TRUE, 'مجمع إهلاك وسائل نقل','Accumulated depreciation - vehicles','0002000100150006'),
('120304', '1203', 'ASSET','CREDIT',TRUE, 'مجمع إهلاك أثاث ومعدات مكتبية','Accumulated depreciation - furniture','0002000100150009'),
('120305', '1203', 'ASSET','CREDIT',TRUE, 'مجمع إهلاك أجهزة حاسب آلي','Accumulated depreciation - computers','0002000100150012'),
('1204',   '12',   'ASSET','DEBIT', FALSE,'أصول غير ملموسة','Intangible assets',NULL),
('120401', '1204', 'ASSET','DEBIT', TRUE, 'برامج حاسب آلي','Software','0001000100010010'),
('120402', '1204', 'ASSET','CREDIT',TRUE, '(-) مجمع إطفاء البرامج','Less: accumulated amortisation - software','0002000100150011'),
('1205',   '12',   'ASSET','DEBIT', TRUE, 'مشروعات تحت التنفيذ','Capital work in progress','000100010002'),
('1206',   '12',   'ASSET','DEBIT', TRUE, 'تأمينات وودائع لدى الغير','Deposits with others','000100020007'),
('2',      NULL,   'LIABILITY','CREDIT',FALSE,'الخصوم','Liabilities','0002'),
('21',     '2',    'LIABILITY','CREDIT',FALSE,'الخصوم المتداولة','Current liabilities','00020001'),
('2101',   '21',   'LIABILITY','CREDIT',FALSE,'الموردون','Suppliers','000200010005'),
('210101', '2101', 'LIABILITY','CREDIT',TRUE, 'موردون محليون','Local suppliers','0002000100050001'),
('210102', '2101', 'LIABILITY','CREDIT',TRUE, 'موردون خارجيون','Foreign suppliers','0002000100050002'),
('2102',   '21',   'LIABILITY','CREDIT',FALSE,'جاري المستثمرين','Investors current accounts',NULL),
('210201', '2102', 'LIABILITY','CREDIT',TRUE, 'جاري المستثمرين','Investors current accounts','0001000200130001'),
('2103',   '21',   'LIABILITY','CREDIT',FALSE,'أرصدة دائنة للعملاء','Customer credit balances',NULL),
('210301', '2103', 'LIABILITY','CREDIT',TRUE, 'مبالغ مستحقة الرد للعملاء','Customer refunds payable',NULL),
('210302', '2103', 'LIABILITY','CREDIT',TRUE, 'دفعات مقدمة من العملاء','Customer advances',NULL),
('2104',   '21',   'LIABILITY','CREDIT',FALSE,'مصروفات مستحقة','Accrued expenses','000200010003'),
('210401', '2104', 'LIABILITY','CREDIT',TRUE, 'أتعاب مراجعة مستحقة','Accrued audit fees','0002000100030001'),
('210402', '2104', 'LIABILITY','CREDIT',TRUE, 'مصروفات مستحقة أخرى','Other accrued expenses',NULL),
('2105',   '21',   'LIABILITY','CREDIT',FALSE,'مستحقات الموظفين','Employee payables','000200010006'),
('210501', '2105', 'LIABILITY','CREDIT',TRUE, 'رواتب مستحقة','Salaries payable','0002000100060001'),
('210502', '2105', 'LIABILITY','CREDIT',TRUE, 'إجازات مستحقة','Leave pay payable','0002000100060002'),
('210503', '2105', 'LIABILITY','CREDIT',TRUE, 'تذاكر سفر مستحقة','Air tickets payable','0002000100060003'),
('2106',   '21',   'LIABILITY','CREDIT',FALSE,'أوراق الدفع','Notes payable','000200010010'),
('210601', '2106', 'LIABILITY','CREDIT',TRUE, 'شيكات تحت الدفع','Cheques payable','0002000100100001'),
('2107',   '21',   'LIABILITY','CREDIT',TRUE, 'الزكاة وضرائب الشركات المستحقة','Zakat and corporate levies payable','0002000100150017'),
('2108',   '21',   'LIABILITY','CREDIT',TRUE, 'الجزء المتداول من القروض','Current portion of loans',NULL),
('2109',   '21',   'LIABILITY','CREDIT',TRUE, 'دائنون متنوعون','Sundry creditors','000200010013'),
('22',     '2',    'LIABILITY','CREDIT',FALSE,'الخصوم غير المتداولة','Non-current liabilities','00020002'),
('2201',   '22',   'LIABILITY','CREDIT',TRUE, 'قروض طويلة الأجل','Long-term loans','000200010014'),
('2202',   '22',   'LIABILITY','CREDIT',TRUE, 'مخصص مكافأة نهاية الخدمة','End-of-service benefits provision','0002000100150002'),
('3',      NULL,   'EQUITY','CREDIT',FALSE,'حقوق الملكية','Equity','0005'),
('3101',   '3',    'EQUITY','CREDIT',FALSE,'رأس المال','Share capital','00050001'),
('310101', '3101', 'EQUITY','CREDIT',TRUE, 'رأس المال المدفوع','Paid-up capital','0002000100040002'),
('3201',   '3',    'EQUITY','CREDIT',TRUE, 'الاحتياطي القانوني','Statutory reserve','0002000100010004'),
('3202',   '3',    'EQUITY','CREDIT',TRUE, 'احتياطيات أخرى','Other reserves','0002000100010003'),
('3301',   '3',    'EQUITY','CREDIT',FALSE,'الأرباح المبقاة','Retained earnings',NULL),
('330101', '3301', 'EQUITY','CREDIT',TRUE, 'الأرباح المبقاة المرحلة','Retained earnings brought forward','3200'),
('3401',   '3',    'EQUITY','CREDIT',FALSE,'جاري الشركاء','Partners current accounts','000200010001'),
('340101', '3401', 'EQUITY','CREDIT',TRUE, 'جاري الشريك - علوش','Partner current account - Alloush','0002000100010001'),
('340102', '3401', 'EQUITY','CREDIT',TRUE, 'جاري الشريك 2','Partner current account - 2','0002000100010002'),
('4',      NULL,   'REVENUE','CREDIT',FALSE,'الإيرادات','Revenue','0004'),
('41',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات المبيعات','Sales revenue',NULL),
('4101',   '41',   'REVENUE','CREDIT',TRUE, 'مبيعات التقسيط','Instalment sales','00040005'),
('4102',   '41',   'REVENUE','CREDIT',TRUE, 'مبيعات نقدية','Cash sales',NULL),
('42',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات التمويل','Finance income',NULL),
('4201',   '42',   'REVENUE','CREDIT',TRUE, 'أرباح التقسيط المحققة','Earned instalment income',NULL),
('43',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات إدارة المستثمرين','Investor management income',NULL),
('4301',   '43',   'REVENUE','CREDIT',TRUE, 'عمولات وأتعاب إدارة المستثمرين','Investor management fees',NULL),
('44',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات رسوم وخدمات','Fee and service income',NULL),
('4401',   '44',   'REVENUE','CREDIT',TRUE, 'إيرادات عقود','Contract income','00040003'),
('4402',   '44',   'REVENUE','CREDIT',TRUE, 'إيرادات تسديد عملاء','Customer settlement income','00040001'),
('4403',   '44',   'REVENUE','CREDIT',TRUE, 'إيرادات استرداد أتعاب قضائية','Recovered legal fees','0003000300040020'),
('45',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات أخرى','Other income',NULL),
('4501',   '45',   'REVENUE','CREDIT',TRUE, 'إيرادات أخرى','Other income','00040006'),
('4502',   '45',   'REVENUE','CREDIT',TRUE, 'خصم مكتسب','Discount received',NULL),
('4503',   '45',   'REVENUE','CREDIT',TRUE, 'أرباح بيع أصول ثابتة','Gain on disposal of assets',NULL),
('49',     '4',    'REVENUE','DEBIT', FALSE,'(-) خصومات ومخالصات ممنوحة','Less: discounts and settlements granted',NULL),
('4901',   '49',   'REVENUE','DEBIT', TRUE, 'خصومات مخالصات العقود','Contract settlement discounts','0003000200030002'),
('5',      NULL,   'EXPENSE','DEBIT', FALSE,'تكلفة الإيرادات','Cost of revenue',NULL),
('51',     '5',    'EXPENSE','DEBIT', FALSE,'تكلفة المبيعات','Cost of sales',NULL),
('5101',   '51',   'EXPENSE','DEBIT', TRUE, 'تكلفة البضاعة المباعة','Cost of goods sold',NULL),
('5102',   '51',   'EXPENSE','DEBIT', TRUE, 'مشتريات بضاعة','Purchases of goods','00030003000400330002'),
('5103',   '51',   'EXPENSE','DEBIT', TRUE, 'مصاريف شراء ونقل بضاعة','Purchase and freight costs',NULL),
('6',      NULL,   'EXPENSE','DEBIT', FALSE,'المصروفات التشغيلية','Operating expenses','0003'),
('61',     '6',    'EXPENSE','DEBIT', FALSE,'الرواتب والأجور وما في حكمها','Salaries, wages and benefits',NULL),
('6101',   '61',   'EXPENSE','DEBIT', TRUE, 'رواتب أساسية','Basic salaries','0003000100010001'),
('6102',   '61',   'EXPENSE','DEBIT', TRUE, 'بدلات','Allowances',NULL),
('6103',   '61',   'EXPENSE','DEBIT', TRUE, 'مكافآت وحوافز','Bonuses and incentives',NULL),
('6104',   '61',   'EXPENSE','DEBIT', TRUE, 'تأمينات اجتماعية','Social insurance',NULL),
('6105',   '61',   'EXPENSE','DEBIT', TRUE, 'مكافأة نهاية الخدمة','End-of-service benefits',NULL),
('6106',   '61',   'EXPENSE','DEBIT', TRUE, 'إقامات وتأشيرات','Residency and visas',NULL),
('6107',   '61',   'EXPENSE','DEBIT', TRUE, 'علاج وتأمين صحي','Medical and health insurance',NULL),
('62',     '6',    'EXPENSE','DEBIT', FALSE,'الإيجارات والمرافق','Rent and utilities',NULL),
('6201',   '62',   'EXPENSE','DEBIT', TRUE, 'إيجارات','Rent','0003000100040003'),
('6202',   '62',   'EXPENSE','DEBIT', TRUE, 'كهرباء ومياه','Electricity and water',NULL),
('6203',   '62',   'EXPENSE','DEBIT', TRUE, 'هاتف واتصالات وبريد','Telephone, communications and post',NULL),
('63',     '6',    'EXPENSE','DEBIT', FALSE,'الصيانة والمحروقات','Maintenance and fuel',NULL),
('6301',   '63',   'EXPENSE','DEBIT', TRUE, 'صيانة سيارات ومحروقات','Vehicle maintenance and fuel',NULL),
('6302',   '63',   'EXPENSE','DEBIT', TRUE, 'صيانة مباني وأثاث','Building and furniture maintenance',NULL),
('6303',   '63',   'EXPENSE','DEBIT', TRUE, 'صيانة برامج وأجهزة','IT maintenance',NULL),
('6304',   '63',   'EXPENSE','DEBIT', TRUE, 'تأمين سيارات','Vehicle insurance',NULL),
('64',     '6',    'EXPENSE','DEBIT', FALSE,'الإهلاك والإطفاء','Depreciation and amortisation',NULL),
('6401',   '64',   'EXPENSE','DEBIT', TRUE, 'مصروف الإهلاك','Depreciation expense',NULL),
('6402',   '64',   'EXPENSE','DEBIT', TRUE, 'مصروف الإطفاء','Amortisation expense',NULL),
('65',     '6',    'EXPENSE','DEBIT', FALSE,'مصاريف التحصيل والقضايا','Collection and legal expenses',NULL),
('6501',   '65',   'EXPENSE','DEBIT', TRUE, 'أتعاب محاماة','Lawyer fees',NULL),
('6502',   '65',   'EXPENSE','DEBIT', TRUE, 'رسوم تنفيذ وقضايا','Enforcement and court fees',NULL),
('66',     '6',    'EXPENSE','DEBIT', FALSE,'الخسائر الائتمانية','Credit losses',NULL),
('6601',   '66',   'EXPENSE','DEBIT', TRUE, 'مصروف الخسائر الائتمانية المتوقعة','Expected credit loss expense',NULL),
('6602',   '66',   'EXPENSE','DEBIT', TRUE, 'ديون معدومة ومخالصات عملاء متعثرين','Bad debts and defaulted-customer settlements',NULL),
('67',     '6',    'EXPENSE','DEBIT', FALSE,'مصاريف البيع والتسويق','Selling and marketing expenses',NULL),
('6701',   '67',   'EXPENSE','DEBIT', TRUE, 'عمولات بيع','Sales commissions',NULL),
('6702',   '67',   'EXPENSE','DEBIT', TRUE, 'دعاية وإعلان','Advertising',NULL),
('68',     '6',    'EXPENSE','DEBIT', FALSE,'مصاريف عمومية وإدارية','General and administrative expenses',NULL),
('6801',   '68',   'EXPENSE','DEBIT', TRUE, 'مصروفات عمومية متنوعة','Sundry general expenses','0003000300040031'),
('6802',   '68',   'EXPENSE','DEBIT', TRUE, 'قرطاسية ومطبوعات','Stationery and printing',NULL),
('6803',   '68',   'EXPENSE','DEBIT', TRUE, 'ضيافة ونظافة','Hospitality and cleaning',NULL),
('6804',   '68',   'EXPENSE','DEBIT', TRUE, 'رسوم حكومية واشتراكات','Government fees and subscriptions',NULL),
('6805',   '68',   'EXPENSE','DEBIT', TRUE, 'عمولات بنكية','Bank charges',NULL),
('6806',   '68',   'EXPENSE','DEBIT', TRUE, 'أتعاب مهنية واستشارات','Professional and consulting fees',NULL),
('6807',   '68',   'EXPENSE','DEBIT', TRUE, 'خسائر بيع أصول ثابتة','Loss on disposal of assets',NULL),
('6808',   '68',   'EXPENSE','DEBIT', TRUE, 'تبرعات','Donations',NULL),
('6899',   '68',   'EXPENSE','DEBIT', TRUE, 'مصروفات أخرى','Other expenses','0003000100040029'),
('69',     '6',    'EXPENSE','DEBIT', FALSE,'الزكاة والضرائب','Zakat and taxes',NULL),
('6901',   '69',   'EXPENSE','DEBIT', TRUE, 'الزكاة وضرائب الشركات','Zakat and corporate levies',NULL);
UPDATE fin_rule_line l
   SET account_derivation_value = s.code
  FROM seed_coa s
 WHERE l.account_derivation_type_code = 'CONSTANT'
   AND s.old_code IS NOT NULL
   AND l.account_derivation_value = s.old_code;
UPDATE fin_account a
   SET code = s.code, name_ar = s.name_ar, name_en = s.name_en,
       account_type_code = s.type_code, nature_code = s.nature_code,
       is_leaf_fl = s.is_leaf_fl, is_active_fl = TRUE,
       updated_by = 'SYSTEM', updated_at = now()
  FROM seed_coa s
 WHERE s.old_code IS NOT NULL AND a.code = s.old_code;
INSERT INTO fin_account (account_pk, code, name_ar, name_en, account_type_code, nature_code,
                         parent_account_id, is_leaf_fl, is_active_fl, created_by, created_at)
SELECT nextval('seq_fin_account'), s.code, s.name_ar, s.name_en, s.type_code, s.nature_code,
       NULL, s.is_leaf_fl, TRUE, 'SYSTEM', now()
  FROM seed_coa s
 WHERE NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = s.code);
UPDATE fin_account a
   SET parent_account_id = p.account_pk
  FROM seed_coa s LEFT JOIN fin_account p ON p.code = s.parent_code
 WHERE a.code = s.code
   AND a.parent_account_id IS DISTINCT FROM p.account_pk;
UPDATE fin_rule_line l
   SET account_derivation_value = '6602'
  FROM fin_event_type_rule r
 WHERE r.event_type_rule_pk = l.event_type_rule_id
   AND r.event_type_code IN ('COMPLAINT_SETTLEMENT', 'COMPLAINT_SETTLEMENT_REVERSED')
   AND l.account_derivation_type_code = 'CONSTANT'
   AND l.account_derivation_value = '4901';
CREATE TEMP TABLE coa_referenced ON COMMIT DROP AS
SELECT account_id AS account_pk FROM fin_journal_line
UNION SELECT account_id FROM fin_account_mapping
UNION SELECT account_id FROM fin_recurring_template_line
UNION SELECT source_account_id FROM fin_allocation_rule
UNION SELECT target_account_id FROM fin_allocation_target
UNION SELECT a.account_pk FROM fin_rule_line l JOIN fin_account a ON a.code = l.account_derivation_value
       WHERE l.account_derivation_type_code = 'CONSTANT'
UNION SELECT account_pk FROM fin_account WHERE is_retained_earnings_fl;
DO $$
DECLARE n INT; total INT := 0;
BEGIN
  LOOP
    DELETE FROM fin_account a
     WHERE NOT EXISTS (SELECT 1 FROM seed_coa s WHERE s.code = a.code)
       AND NOT EXISTS (SELECT 1 FROM coa_referenced r WHERE r.account_pk = a.account_pk)
       AND NOT EXISTS (SELECT 1 FROM fin_account c WHERE c.parent_account_id = a.account_pk);
    GET DIAGNOSTICS n = ROW_COUNT;
    total := total + n;
    EXIT WHEN n = 0;
  END LOOP;
  RAISE NOTICE 'V40: % legacy template accounts deleted', total;
END $$;
DO $$
DECLARE r RECORD; v_arch BIGINT; v_root VARCHAR(1); n INT := 0;
BEGIN
  FOR r IN SELECT a.account_pk, a.account_type_code FROM fin_account a
            WHERE NOT EXISTS (SELECT 1 FROM seed_coa s WHERE s.code = a.code)
              AND a.code NOT IN ('1999', '2999', '3999', '4999', '6999') LOOP
    v_root := CASE r.account_type_code WHEN 'ASSET' THEN '1' WHEN 'LIABILITY' THEN '2' WHEN 'EQUITY' THEN '3'
                                       WHEN 'REVENUE' THEN '4' ELSE '6' END;
    SELECT account_pk INTO v_arch FROM fin_account WHERE code = v_root || '999';
    IF v_arch IS NULL THEN
      v_arch := nextval('seq_fin_account');
      INSERT INTO fin_account (account_pk, code, name_ar, name_en, account_type_code, nature_code,
                               parent_account_id, is_leaf_fl, is_active_fl, created_by, created_at)
      SELECT v_arch, v_root || '999', 'حسابات مؤرشفة (غير مستخدمة)', 'Archived accounts (not in use)',
             p.account_type_code, p.nature_code, p.account_pk, FALSE, FALSE, 'SYSTEM', now()
        FROM fin_account p WHERE p.code = v_root;
    END IF;
    UPDATE fin_account SET parent_account_id = v_arch, is_active_fl = FALSE,
                           updated_by = 'SYSTEM', updated_at = now()
     WHERE account_pk = r.account_pk;
    n := n + 1;
  END LOOP;
  RAISE NOTICE 'V40: % referenced legacy accounts archived (inactive)', n;
END $$;
DO $$
DECLARE v_list TEXT; v_count INT;
BEGIN
  SELECT count(*) INTO v_count FROM seed_coa s WHERE NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = s.code);
  IF v_count > 0 THEN RAISE EXCEPTION 'V40 6.1: % target accounts missing', v_count; END IF;
  SELECT string_agg(a.code, ', ') INTO v_list FROM fin_account a JOIN seed_coa s ON s.code = a.code
   WHERE a.parent_account_id IS DISTINCT FROM (SELECT p.account_pk FROM fin_account p WHERE p.code = s.parent_code);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V40 6.2: wrong parent: %', v_list; END IF;
  SELECT string_agg(a.code, ', ') INTO v_list FROM fin_account a
   WHERE a.is_leaf_fl AND EXISTS (SELECT 1 FROM fin_account c WHERE c.parent_account_id = a.account_pk);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V40 6.3: leaf accounts with children: %', v_list; END IF;
  SELECT string_agg(c.code, ', ') INTO v_list FROM fin_account c JOIN fin_account p ON p.account_pk = c.parent_account_id
   WHERE c.account_type_code <> p.account_type_code;
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V40 6.4: child/parent account type mismatch: %', v_list; END IF;
  SELECT count(*) INTO v_count FROM fin_account WHERE is_retained_earnings_fl AND code = '330101' AND is_leaf_fl AND is_active_fl;
  IF v_count <> 1 THEN RAISE EXCEPTION 'V40 6.5: retained earnings is not the active leaf 330101'; END IF;
  SELECT string_agg(r.event_type_code || ':' || l.line_no || '=' || COALESCE(l.account_derivation_value, 'NULL'), ', ') INTO v_list
    FROM fin_rule_line l JOIN fin_event_type_rule r ON r.event_type_rule_pk = l.event_type_rule_id
   WHERE r.is_active_fl AND l.account_derivation_type_code = 'CONSTANT'
     AND NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = l.account_derivation_value AND a.is_leaf_fl AND a.is_active_fl);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V40 6.6: active rule lines on a missing / non-leaf / inactive account: %', v_list; END IF;
  SELECT string_agg(m.event_type_code || '/' || m.business_value, ', ') INTO v_list
    FROM fin_account_mapping m JOIN fin_account a ON a.account_pk = m.account_id
   WHERE m.is_active_fl AND NOT (a.is_leaf_fl AND a.is_active_fl);
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V40 6.7: active mappings on a non-leaf / inactive account: %', v_list; END IF;
  SELECT count(*) INTO v_count FROM fin_account WHERE parent_account_id IS NULL AND code NOT IN ('1','2','3','4','5','6');
  IF v_count > 0 THEN RAISE EXCEPTION 'V40 6.8: % root accounts outside the six classes', v_count; END IF;
  RAISE NOTICE 'V40: chart restructured — % accounts (% active leaves)',
    (SELECT count(*) FROM fin_account), (SELECT count(*) FROM fin_account WHERE is_leaf_fl AND is_active_fl);
END $$;
-- ===== V41
UPDATE fin_account
   SET nature_code = 'DEBIT', updated_by = 'SYSTEM', updated_at = now()
 WHERE account_type_code = 'ASSET' AND nature_code = 'CREDIT'
   AND code IN ('110203', '110204', '1203', '120302', '120303', '120304', '120305', '120402');
UPDATE fin_account
   SET nature_code = 'CREDIT', updated_by = 'SYSTEM', updated_at = now()
 WHERE account_type_code = 'REVENUE' AND nature_code = 'DEBIT'
   AND code IN ('49', '4901');
DO $$
DECLARE v_list TEXT;
BEGIN
  SELECT string_agg(code, ', ') INTO v_list FROM fin_account
   WHERE (account_type_code IN ('ASSET', 'EXPENSE') AND nature_code <> 'DEBIT')
      OR (account_type_code IN ('LIABILITY', 'EQUITY', 'REVENUE') AND nature_code <> 'CREDIT');
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V41: accounts whose nature differs from their class: %', v_list; END IF;
END $$;
-- ===== V42
CREATE TEMP TABLE reset_entries ON COMMIT DROP AS
SELECT journal_entry_pk FROM fin_journal_entry WHERE doc_date < DATE '2026-10-01';
UPDATE fin_journal_entry SET original_entry_id = NULL
 WHERE original_entry_id IN (SELECT journal_entry_pk FROM reset_entries)
   AND journal_entry_pk NOT IN (SELECT journal_entry_pk FROM reset_entries);
UPDATE fin_journal_entry SET reversal_entry_id = NULL
 WHERE reversal_entry_id IN (SELECT journal_entry_pk FROM reset_entries)
   AND journal_entry_pk NOT IN (SELECT journal_entry_pk FROM reset_entries);
UPDATE fin_journal_entry SET original_entry_id = NULL, reversal_entry_id = NULL
 WHERE journal_entry_pk IN (SELECT journal_entry_pk FROM reset_entries);
DELETE FROM fin_journal_line_dim
 WHERE journal_line_id IN (SELECT l.journal_line_pk FROM fin_journal_line l
                            WHERE l.journal_entry_id IN (SELECT journal_entry_pk FROM reset_entries));
DELETE FROM fin_journal_line WHERE journal_entry_id IN (SELECT journal_entry_pk FROM reset_entries);
DELETE FROM fin_journal_entry WHERE journal_entry_pk IN (SELECT journal_entry_pk FROM reset_entries);
DELETE FROM fin_fiscal_period p
 WHERE p.end_date < DATE '2026-10-01'
   AND NOT EXISTS (SELECT 1 FROM fin_journal_entry e WHERE e.period_id = p.fiscal_period_pk);
DELETE FROM fin_fiscal_year y
 WHERE y.end_date < DATE '2026-10-01'
   AND NOT EXISTS (SELECT 1 FROM fin_fiscal_period p WHERE p.fiscal_year_id = y.fiscal_year_pk)
   AND NOT EXISTS (SELECT 1 FROM fin_journal_entry e WHERE e.fiscal_year_id = y.fiscal_year_pk);
UPDATE fin_fiscal_year y
   SET start_date = (SELECT min(p.start_date) FROM fin_fiscal_period p WHERE p.fiscal_year_id = y.fiscal_year_pk),
       updated_by = 'SYSTEM', updated_at = now()
 WHERE y.code = '2026' AND y.start_date < DATE '2026-10-01'
   AND EXISTS (SELECT 1 FROM fin_fiscal_period p WHERE p.fiscal_year_id = y.fiscal_year_pk);
DO $$
DECLARE n INT; v_start DATE;
BEGIN
  SELECT count(*) INTO n FROM fin_journal_entry WHERE doc_date < DATE '2026-10-01';
  IF n > 0 THEN RAISE EXCEPTION 'V42: % journal entries before 2026-10-01 remain', n; END IF;
  SELECT count(*) INTO n FROM fin_fiscal_period WHERE end_date < DATE '2026-10-01';
  IF n > 0 THEN RAISE EXCEPTION 'V42: % fiscal periods before 2026-10-01 remain', n; END IF;
  SELECT start_date INTO v_start FROM fin_fiscal_year WHERE code = '2026';
  IF v_start IS NOT NULL AND v_start <> DATE '2026-10-01' THEN
    RAISE EXCEPTION 'V42: fiscal year 2026 starts % (expected 2026-10-01)', v_start;
  END IF;
  SELECT count(*) INTO n FROM fin_fiscal_period p JOIN fin_fiscal_year y ON y.fiscal_year_pk = p.fiscal_year_id WHERE y.code = '2026';
  IF v_start IS NOT NULL AND n <> 3 THEN RAISE EXCEPTION 'V42: fiscal year 2026 has % periods (expected 3: Oct, Nov, Dec)', n; END IF;
  RAISE NOTICE 'V42: ledger reset — % entries remain (all on/after 2026-10-01), fiscal year 2026 = % periods from %',
    (SELECT count(*) FROM fin_journal_entry), n, v_start;
END $$;
-- ===== V43
CREATE TEMP TABLE coa_keep (
  code         VARCHAR(30) PRIMARY KEY,
  parent_code  VARCHAR(30),
  type_code    VARCHAR(20) NOT NULL,
  nature_code  VARCHAR(10) NOT NULL,
  is_leaf_fl   BOOLEAN     NOT NULL,
  name_ar      VARCHAR(200) NOT NULL,
  name_en      VARCHAR(200) NOT NULL,
  from_code    VARCHAR(30)
) ON COMMIT DROP;
INSERT INTO coa_keep VALUES
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
('3',      NULL,   'EQUITY','CREDIT',FALSE,'حقوق الملكية','Equity',NULL),
('3101',   '3',    'EQUITY','CREDIT',FALSE,'رأس المال','Share capital',NULL),
('310101', '3101', 'EQUITY','CREDIT',TRUE, 'رأس المال','Share capital',NULL),
('3301',   '3',    'EQUITY','CREDIT',FALSE,'الأرباح المرحلة','Retained earnings',NULL),
('330101', '3301', 'EQUITY','CREDIT',TRUE, 'أرباح مرحلة','Retained earnings brought forward',NULL),
('3501',   '3',    'EQUITY','CREDIT',TRUE, 'تسوية فروقات / عجز','Reconciliation differences / shortage',NULL),
('4',      NULL,   'REVENUE','CREDIT',FALSE,'الإيرادات','Revenue',NULL),
('41',     '4',    'REVENUE','CREDIT',FALSE,'المبيعات','Sales',NULL),
('4101',   '41',   'REVENUE','CREDIT',TRUE, 'المبيعات','Sales',NULL),
('44',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات الشكاوى والخدمات','Complaint and service income',NULL),
('4401',   '44',   'REVENUE','CREDIT',TRUE, 'إيرادات شكوى / خدمات','Complaint / service income',NULL),
('4403',   '44',   'REVENUE','CREDIT',TRUE, 'إيرادات استرداد أتعاب قضائية','Recovered legal fees',NULL),
('45',     '4',    'REVENUE','CREDIT',FALSE,'إيرادات أخرى','Other income',NULL),
('4501',   '45',   'REVENUE','CREDIT',TRUE, 'إيرادات أخرى','Other income',NULL),
('4504',   '45',   'REVENUE','CREDIT',TRUE, 'إيرادات أرباح البنوك','Bank profit income',NULL),
('5',      NULL,   'EXPENSE','DEBIT', FALSE,'تكلفة المبيعات','Cost of sales',NULL),
('51',     '5',    'EXPENSE','DEBIT', FALSE,'المشتريات','Purchases',NULL),
('5102',   '51',   'EXPENSE','DEBIT', TRUE, 'المشتريات','Purchases',NULL),
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
UPDATE fin_rule_line l
   SET account_derivation_value = '210201'
  FROM fin_event_type_rule r
 WHERE r.event_type_rule_pk = l.event_type_rule_id
   AND r.event_type_code IN ('CONTRACT_SETTLEMENT', 'CONTRACT_SETTLEMENT_REVERSED',
                             'COMPLAINT_SETTLEMENT', 'COMPLAINT_SETTLEMENT_REVERSED')
   AND l.account_derivation_type_code = 'CONSTANT'
   AND l.account_derivation_value IN ('4901', '6602');
UPDATE fin_account_mapping m
   SET account_id = (SELECT account_pk FROM fin_account WHERE code = '4401'), updated_by = 'SYSTEM', updated_at = now()
 WHERE m.account_id = (SELECT account_pk FROM fin_account WHERE code = '4402');
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
  SELECT string_agg(r.event_type_code, ', ') INTO v_list
    FROM fin_event_type_rule r
   WHERE r.event_type_code IN ('CONTRACT_SETTLEMENT', 'COMPLAINT_SETTLEMENT')
     AND NOT EXISTS (SELECT 1 FROM fin_rule_line l WHERE l.event_type_rule_id = r.event_type_rule_pk
                       AND l.direction_code = 'DEBIT' AND l.account_derivation_value = '210201');
  IF v_list IS NOT NULL THEN RAISE EXCEPTION 'V43: settlement rules not debiting 210201: %', v_list; END IF;
  RAISE NOTICE 'V43: chart now % accounts (% postable leaves)',
    (SELECT count(*) FROM fin_account), (SELECT count(*) FROM fin_account WHERE is_leaf_fl AND is_active_fl);
END $$;
-- ===== Flyway history (checksums computed exactly as Flyway does; verified against a Flyway-applied database)
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
SELECT r.rank0 + x.n, x.v, x.d, 'SQL', x.s, x.c, current_user, now(), 0, TRUE
  FROM (SELECT max(installed_rank) AS rank0 FROM flyway_schema_history) r,
       (VALUES (1, '40', 'fin chart of accounts restructure',             'V40__fin_chart_of_accounts_restructure.sql',             1734171238),
               (2, '41', 'fin contra accounts nature',                    'V41__fin_contra_accounts_nature.sql',                    624555822),
               (3, '42', 'fin go live reset before 2026 10',              'V42__fin_go_live_reset_before_2026_10.sql',              1329449903),
               (4, '43', 'fin accountant notes rules and chart cleanup',  'V43__fin_accountant_notes_rules_and_chart_cleanup.sql',  1552489227)) x(n, v, d, s, c);
-- ===== opening balances (draft numbers)
CREATE TEMP TABLE ob (branch VARCHAR(5), line_no INT, code VARCHAR(30), dir VARCHAR(6), amount NUMERIC(18,3)) ON COMMIT DROP;
INSERT INTO ob VALUES
('1', 1,'110101','DEBIT',    256.176), ('1', 2,'110113','DEBIT',  47629.430), ('1', 3,'120204','DEBIT',      1.000),
('1', 4,'110309','DEBIT',   9543.824), ('1', 5,'110401','DEBIT',   5952.000), ('1', 6,'110307','DEBIT',    308.748),
('1', 7,'110304','DEBIT',   2450.000), ('1', 8,'110310','DEBIT', 195016.065),
('1', 9,'210201','CREDIT',210654.251), ('1',10,'211001','CREDIT', 14938.670), ('1',11,'211002','CREDIT',  3398.000),
('1',12,'211003','CREDIT',  2865.000), ('1',13,'211004','CREDIT', 12179.287), ('1',14,'3501',  'CREDIT', 17122.035),
('2', 1,'110101','DEBIT',   1392.755), ('2', 2,'110113','DEBIT',  68365.153), ('2', 3,'120204','DEBIT',      1.000),
('2', 4,'110309','DEBIT',  26444.104), ('2', 5,'110401','DEBIT',   4752.000), ('2', 6,'110310','DEBIT', 282273.920),
('2', 7,'210201','CREDIT',310809.199), ('2', 8,'211001','CREDIT', 34385.100), ('2', 9,'211002','CREDIT',  5845.500),
('2',10,'211003','CREDIT', 16502.000), ('2',11,'3501',  'CREDIT', 15687.133),
('3', 1,'110101','DEBIT',    262.260), ('3', 2,'110111','DEBIT',    236.538), ('3', 3,'120204','DEBIT',      1.000),
('3', 4,'110307','DEBIT',   3080.000), ('3', 5,'110310','DEBIT', 104865.388), ('3', 6,'3501',  'DEBIT',   3246.651),
('3', 7,'210201','CREDIT',104761.737), ('3', 8,'211001','CREDIT',  4549.600), ('3', 9,'211002','CREDIT',   795.000),
('3',10,'211003','CREDIT',  1585.500);
DO $$
DECLARE
  b RECORD; v_year BIGINT; v_period BIGINT; v_entry BIGINT; v_line BIGINT; v_seq INT;
  v_org_dim BIGINT; v_br_dim BIGINT; v_org_val BIGINT; v_br_val BIGINT; v_diff NUMERIC; v_missing TEXT;
BEGIN
  IF EXISTS (SELECT 1 FROM fin_journal_entry WHERE journal_type_code = 'OPENING' AND doc_date = DATE '2026-10-01') THEN
    RAISE NOTICE 'V44: opening balances already posted'; RETURN;
  END IF;
  SELECT string_agg(DISTINCT o.code, ', ') INTO v_missing FROM ob o
   WHERE NOT EXISTS (SELECT 1 FROM fin_account a WHERE a.code = o.code AND a.is_leaf_fl AND a.is_active_fl);
  IF v_missing IS NOT NULL THEN RAISE EXCEPTION 'V44: accounts missing / not postable: %', v_missing; END IF;
  FOR b IN SELECT branch, SUM(CASE dir WHEN 'DEBIT' THEN amount ELSE -amount END) diff FROM ob GROUP BY branch LOOP
    IF b.diff <> 0 THEN RAISE EXCEPTION 'V44: branch % opening entry is out of balance by %', b.branch, b.diff; END IF;
  END LOOP;
  SELECT y.fiscal_year_pk, p.fiscal_period_pk INTO v_year, v_period
    FROM fin_fiscal_year y JOIN fin_fiscal_period p ON p.fiscal_year_id = y.fiscal_year_pk
   WHERE DATE '2026-10-01' BETWEEN p.start_date AND p.end_date;
  IF v_period IS NULL THEN RAISE EXCEPTION 'V44: no fiscal period covers 2026-10-01'; END IF;
  SELECT dimension_pk INTO v_org_dim FROM fin_dimension WHERE code = 'ORG';
  SELECT dimension_pk INTO v_br_dim  FROM fin_dimension WHERE code = 'BRANCH';
  SELECT dimension_value_pk INTO v_org_val FROM fin_dimension_value WHERE dimension_id = v_org_dim AND code = '1';
  SELECT COALESCE(MAX(substring(doc_no FROM 'JV-2026-([0-9]+)')::INT), 0) INTO v_seq
    FROM fin_journal_entry WHERE fiscal_year_id = v_year;
  FOR b IN SELECT DISTINCT branch FROM ob ORDER BY branch LOOP
    SELECT dimension_value_pk INTO v_br_val FROM fin_dimension_value WHERE dimension_id = v_br_dim AND code = b.branch;
    IF v_br_val IS NULL THEN RAISE EXCEPTION 'V44: BRANCH % not defined', b.branch; END IF;
    v_seq := v_seq + 1;
    v_entry := nextval('seq_fin_journal_entry');
    INSERT INTO fin_journal_entry (journal_entry_pk, doc_no, doc_date, fiscal_year_id, period_id, journal_type_code,
                                   status_code, description_ar, description_en, posted_at, created_by, created_at)
    VALUES (v_entry, 'JV-2026-' || lpad(v_seq::text, 6, '0'), DATE '2026-10-01', v_year, v_period, 'OPENING', 'POSTED',
            'الأرصدة الافتتاحية في 30-09-2026 - ' || (SELECT name_ar FROM fin_dimension_value WHERE dimension_value_pk = v_br_val),
            'Opening balances as at 2026-09-30 - branch ' || b.branch, now(), 'SYSTEM', now());
    FOR v_line IN SELECT line_no FROM ob WHERE branch = b.branch ORDER BY line_no LOOP
      INSERT INTO fin_journal_line (journal_line_pk, journal_entry_id, line_no, account_id, amount, direction_code,
                                    is_remainder_fl, description_ar, description_en, created_at)
      SELECT nextval('seq_fin_journal_line'), v_entry, o.line_no, a.account_pk, o.amount, o.dir, FALSE,
             'رصيد افتتاحي', 'Opening balance', now()
        FROM ob o JOIN fin_account a ON a.code = o.code WHERE o.branch = b.branch AND o.line_no = v_line;
      INSERT INTO fin_journal_line_dim (journal_line_dim_pk, journal_line_id, dimension_id, dimension_value_id)
      SELECT nextval('seq_fin_journal_line_dim'), currval('seq_fin_journal_line'), d.dim, d.val
        FROM (VALUES (v_org_dim, v_org_val), (v_br_dim, v_br_val)) d(dim, val);
    END LOOP;
  END LOOP;
  SELECT SUM(CASE l.direction_code WHEN 'DEBIT' THEN l.amount ELSE -l.amount END) INTO v_diff
    FROM fin_journal_line l JOIN fin_journal_entry e ON e.journal_entry_pk = l.journal_entry_id
   WHERE e.journal_type_code = 'OPENING' AND e.doc_date = DATE '2026-10-01';
  IF v_diff <> 0 THEN RAISE EXCEPTION 'V44: opening entries out of balance by %', v_diff; END IF;
  RAISE NOTICE 'V44: 3 opening entries posted on 2026-10-01 (balanced)';
END $$;
-- ===== final check
DO $$ BEGIN
  IF (SELECT SUM(CASE direction_code WHEN 'DEBIT' THEN amount ELSE -amount END) FROM fin_journal_line) <> 0 THEN
    RAISE EXCEPTION 'golive: ledger does not balance';
  END IF;
  RAISE NOTICE 'golive: Flyway now V%, % accounts, % entries (all from 2026-10-01), ledger balanced',
    (SELECT max(version::int) FROM flyway_schema_history), (SELECT count(*) FROM fin_account), (SELECT count(*) FROM fin_journal_entry);
END $$;
COMMIT;
