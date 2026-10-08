-- ============================================================
-- DRAFT — V44 — FIN — opening balances as at 30-09-2026, posted on 2026-10-01
-- ============================================================
-- NOT IN db/migration YET. Moves there (as V44__fin_opening_balances_2026_10_01.sql) once the owner
-- confirms the cash point below. Source: «الارصدة الافتتاحية2.xlsx», sheet «الارصدة الافتتاحية».
-- Branch columns (owner, 2026-10-08): 1st = أفضل وسيط (BRANCH 1), 2nd = الوسيط الذهبي (BRANCH 2),
-- 3rd = الوسائط الفضية (BRANCH 3) — the LOAN_SYS ORGANIZATION_SUB keys, ORG = 1 (شركه الوسائط).
--
-- OPEN POINT — the workbook's totals leave out the cash row (its SUM starts one row below
-- «الصندوق»), and «تسوية فروقات / عجز» was computed to balance THAT total. With cash included each
-- branch is out by exactly its cash balance (256.176 / 1,392.755 / 262.260). This draft balances
-- by moving the reconciliation line by the cash amount:
--   BRANCH 1: تسوية credit 16,865.859 → 17,122.035      BRANCH 2: credit 14,294.378 → 15,687.133
--   BRANCH 3: تسوية debit   3,508.911 →  3,246.651
-- If the accountant's intent is different, only those three figures change.
-- Amounts are KWD rounded to 3 decimals (the sheet carries float noise such as 310809.19899999996).
-- Journal type OPENING (the type FIN's own year-end opening entry uses), one entry per branch,
-- every line tagged ORG 1 + its BRANCH, docNo continues the year's JV-2026-NNNNNN sequence.
-- ============================================================

CREATE TEMP TABLE ob (branch VARCHAR(5), line_no INT, code VARCHAR(30), dir VARCHAR(6), amount NUMERIC(18,3)) ON COMMIT DROP;
INSERT INTO ob VALUES
-- BRANCH 1 — أفضل وسيط
('1', 1,'110101','DEBIT',    256.176), ('1', 2,'110113','DEBIT',  47629.430), ('1', 3,'120204','DEBIT',      1.000),
('1', 4,'110309','DEBIT',   9543.824), ('1', 5,'110401','DEBIT',   5952.000), ('1', 6,'110307','DEBIT',    308.748),
('1', 7,'110304','DEBIT',   2450.000), ('1', 8,'110310','DEBIT', 195016.065),
('1', 9,'210201','CREDIT',210654.251), ('1',10,'211001','CREDIT', 14938.670), ('1',11,'211002','CREDIT',  3398.000),
('1',12,'211003','CREDIT',  2865.000), ('1',13,'211004','CREDIT', 12179.287), ('1',14,'3501',  'CREDIT', 17122.035),
-- BRANCH 2 — الوسيط الذهبي
('2', 1,'110101','DEBIT',   1392.755), ('2', 2,'110113','DEBIT',  68365.153), ('2', 3,'120204','DEBIT',      1.000),
('2', 4,'110309','DEBIT',  26444.104), ('2', 5,'110401','DEBIT',   4752.000), ('2', 6,'110310','DEBIT', 282273.920),
('2', 7,'210201','CREDIT',310809.199), ('2', 8,'211001','CREDIT', 34385.100), ('2', 9,'211002','CREDIT',  5845.500),
('2',10,'211003','CREDIT', 16502.000), ('2',11,'3501',  'CREDIT', 15687.133),
-- BRANCH 3 — الوسائط الفضية
('3', 1,'110101','DEBIT',    262.260), ('3', 2,'110111','DEBIT',    236.538), ('3', 3,'120204','DEBIT',      1.000),
('3', 4,'110307','DEBIT',   3080.000), ('3', 5,'110310','DEBIT', 104865.388), ('3', 6,'3501',  'DEBIT',   3246.651),
('3', 7,'210201','CREDIT',104761.737), ('3', 8,'211001','CREDIT',  4549.600), ('3', 9,'211002','CREDIT',   795.000),
('3',10,'211003','CREDIT',  1585.500);

DO $$
DECLARE
  b RECORD; v_year BIGINT; v_period BIGINT; v_entry BIGINT; v_line BIGINT; v_seq INT;
  v_org_dim BIGINT; v_br_dim BIGINT; v_org_val BIGINT; v_br_val BIGINT; v_diff NUMERIC; v_missing TEXT;
BEGIN
  -- nothing to do if already posted
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
