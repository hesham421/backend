-- ============================================================================
-- 04-compound-settlement-deploy.sql — customer settlements as ONE compound entry   [19c, run on PRODUCTION]
-- ============================================================================
-- Prepared 2026-10-09 (owner's choice: automatic merge on settlement day). Tested on a 19c-shaped
-- schema: contract (payments 300 cash + 200 cheque on the day, discount 10 → one entry 510; discount
-- changed → re-merged 515; a merged payment edited → flagged, not posted; settlement cancelled →
-- reversed and the payments stand alone again) and complaint (1000 cheque + 175 → 1175).
--
-- ORDER — all four steps, in this order, outside working hours (step 2 re-creates the seven event
-- triggers DISABLED and re-enables them once VALID: saves in those seconds emit nothing):
--   1. LOAN_SYS:  sqlplus LOAN_SYS/...@//localhost:1521/ORCLPDB @02-package-19c.sql
--   2. LOAN_SYS:  sqlplus LOAN_SYS/...@//localhost:1521/ORCLPDB @03-triggers-19c.sql
--   3. Postgres:  psql -U postgres -d erp_db -v ON_ERROR_STOP=1
--                   -f governance/project-artifacts/backend/seed-scripts/PENDING-V44__fin_compound_settlement_rules.sql
--                 then move that file back to src/main/resources/db/migration/V44__fin_compound_settlement_rules.sql
--                 and record it (or let the next deploy's Flyway apply it — only AFTER steps 1–2).
--   4. LOAN_SYS:  sqlplus LOAN_SYS/...@//localhost:1521/ORCLPDB @04-compound-settlement-deploy.sql   (this file)
-- Already done on production 2026-10-09: CHK_ACCOUNTING_EVENT_LOG_ACTION accepts 'MERGED'.
-- ============================================================================
SET SERVEROUTPUT ON
WHENEVER SQLERROR EXIT FAILURE

-- the package must be the compound version and the triggers valid
DECLARE n NUMBER;
BEGIN
  SELECT COUNT(*) INTO n FROM user_procedures WHERE object_name = 'PKG_ACCOUNTING_EVENT' AND procedure_name = 'SETTLE';
  IF n = 0 THEN RAISE_APPLICATION_ERROR(-20010, 'run 02-package-19c.sql first'); END IF;
  SELECT COUNT(*) INTO n FROM user_objects WHERE (object_name = 'PKG_ACCOUNTING_EVENT' OR object_name LIKE 'TRG\_AE\_%' ESCAPE '\') AND status <> 'VALID';
  IF n > 0 THEN RAISE_APPLICATION_ERROR(-20011, n || ' invalid objects - run 02 and 03 again'); END IF;
END;
/

-- convert the settlements already posted since go-live (2026-10-01) to the compound form:
-- the old two-line settlement is reversed and the day's payments are absorbed into one entry
BEGIN
  FOR c IN (SELECT * FROM CONTRACT WHERE CONTRACT_FL = 13 AND QUITTANCE_DATE >= DATE '2026-10-01' AND NVL(DISCOUNT, 0) <> 0) LOOP
    PKG_ACCOUNTING_EVENT.unsettle('CONTRACT', c.CONTRACT_PK, 'UPDATE', FALSE);
    PKG_ACCOUNTING_EVENT.settle('CONTRACT', c.CONTRACT_PK, 'UPDATE', c.DISCOUNT, c.QUITTANCE_DATE, c.ORGANIZATION_FK,
      c.ORGANIZATION_SUB_FK, c.CUSTOMER_FK, c.INVESTORS_FK, c.CONTRACT_PK, c.CONTRACT_AUTO_PK, c.NOTE);
    DBMS_OUTPUT.PUT_LINE('contract ' || c.CONTRACT_AUTO_PK || ' converted');
  END LOOP;
  FOR c IN (SELECT * FROM COMPLAINTS WHERE COMPLAINT_FL = 1 AND QUITTANCE_DATE >= DATE '2026-10-01'
              AND COALESCE(NULLIF(COMPLAINT_SETTLEMENT, 0), DISCOUNT, 0) <> 0) LOOP
    PKG_ACCOUNTING_EVENT.unsettle('COMPLAINT', c.COMPLAINT_ID, 'UPDATE', FALSE);
    PKG_ACCOUNTING_EVENT.settle('COMPLAINT', c.COMPLAINT_ID, 'UPDATE', COALESCE(NULLIF(c.COMPLAINT_SETTLEMENT, 0), c.DISCOUNT),
      c.QUITTANCE_DATE, c.ORGANIZATION_FK, c.SUB_ORGANIZATION_FK, c.CUSTOMER_FK, c.INVESTORS_FK, c.CONTRACT_FK,
      TO_CHAR(c.COMPLAINT_NUMBER), c.NOTS);
    DBMS_OUTPUT.PUT_LINE('complaint ' || c.COMPLAINT_NUMBER || ' converted');
  END LOOP;
  COMMIT;
END;
/
SELECT event_log_pk, source_table || ':' || source_pk || ' ' || source_action || ' ' || event_type_code ev,
       JSON_QUERY(payload, '$.amounts') amounts
  FROM ACCOUNTING_EVENT_LOG WHERE created_date > SYSTIMESTAMP - INTERVAL '5' MINUTE ORDER BY event_log_pk;
EXIT
