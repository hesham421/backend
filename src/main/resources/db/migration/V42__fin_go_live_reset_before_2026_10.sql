-- ============================================================
-- V42 — FIN — go-live reset: the ledger starts on 2026-10-01
-- ============================================================
-- Owner's instruction (2026-10-08): the system starts in October 2026; nothing before it matters.
-- Every journal entry dated before 2026-10-01 (the event-generated entries of the shadow run and
-- any test entry) is deleted, and so are the fiscal periods before October. Fiscal year 2026 now runs
-- 2026-10-01 .. 2026-12-31 (periods 10, 11, 12 keep their numbers, so a period still names its
-- month). Opening balances as at 30-09-2026 are posted separately, into October.
-- Deliberate exception to "a posted entry is never deleted, only reversed": this is the go-live cut,
-- not a correction of live books.
-- Consequence downstream: an event whose docDate is before 2026-10-01 (a late edit of a September
-- row in LOAN_SYS) now finds no period and is PARKED by fin-consumer; nothing is lost silently.
-- Idempotent: a second run finds nothing to delete.
-- ============================================================

CREATE TEMP TABLE reset_entries ON COMMIT DROP AS
SELECT journal_entry_pk FROM fin_journal_entry WHERE doc_date < DATE '2026-10-01';

-- entries kept (October onwards) must not point at a deleted one through the reversal links
UPDATE fin_journal_entry SET original_entry_id = NULL
 WHERE original_entry_id IN (SELECT journal_entry_pk FROM reset_entries)
   AND journal_entry_pk NOT IN (SELECT journal_entry_pk FROM reset_entries);
UPDATE fin_journal_entry SET reversal_entry_id = NULL
 WHERE reversal_entry_id IN (SELECT journal_entry_pk FROM reset_entries)
   AND journal_entry_pk NOT IN (SELECT journal_entry_pk FROM reset_entries);
-- and the deleted ones not at each other (self-references block the DELETE otherwise)
UPDATE fin_journal_entry SET original_entry_id = NULL, reversal_entry_id = NULL
 WHERE journal_entry_pk IN (SELECT journal_entry_pk FROM reset_entries);

DELETE FROM fin_journal_line_dim
 WHERE journal_line_id IN (SELECT l.journal_line_pk FROM fin_journal_line l
                            WHERE l.journal_entry_id IN (SELECT journal_entry_pk FROM reset_entries));
DELETE FROM fin_journal_line WHERE journal_entry_id IN (SELECT journal_entry_pk FROM reset_entries);
DELETE FROM fin_journal_entry WHERE journal_entry_pk IN (SELECT journal_entry_pk FROM reset_entries);

-- periods before October, then any whole fiscal year that ended before it
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
