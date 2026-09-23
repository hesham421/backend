# HANDOVER — Oracle `LOAN_SYS` accounting cutover to FIN

**As of:** 2026-09-23 evening · **Owner of record:** heshamezzat60@gmail.com · **Prepared by:** Claude (session of 2026-09-23)
**Purpose of this file:** anyone can pick this up on any later day and continue without the original session. Everything below is verifiable from the files in this folder and the databases named.

> **الخلاصة بالعربي:** المحاسبة القديمة داخل تريجرات Oracle **موقوفة فعلاً** منذ 2026-09-23 14:17 UTC على نسخة الاختبار (وهي نسخة الإنتاج). قاعدة FIN على PostgreSQL مهيأة (v1). ملفات إرسال الأحداث من Oracle **مكتوبة ولم تُنفَّذ**. الباقي: بناء FIN v2، تفعيل القواعد، تشغيل ملفات Oracle 1→3، بناء الـ consumer، ثم replay 2026. من لحظة الإيقاف كل عملية تجارية تُحفظ هي دين على الـ replay.

---

## 1. Environments

| | Where | State now |
|---|---|---|
| **Oracle (legacy)** | `LOAN_SYS@localhost:1521/FREEPDB1` — Oracle AI Database 26ai Free 23.26.2.0.0, `compatible=23.6.0`. **Production copy used for test** (user statement 2026-09-23). Credentials `LOAN_SYS`/`LOAN_SYS`, user holds `DBA` | legacy accounting **stopped** (§3); no queue, no emit package, no emit triggers yet |
| **PostgreSQL (FIN)** | `erp_db@localhost:5432` (Docker `erp-backend-postgres`, PG 17); app jar on `:7272`; Flyway at **v34** (FIN v1) | reset + seeded 2026-09-23 (§3); v2 schema **not** built |
| **Governance** | `governance/shared` submodule at `cbc6a0e` — FIN v2 analysis + packages delivered; `backend/modules/FIN/v2/execution-state.json` = every phase **PENDING**, current `CORE` | |
| **MCP** | `.mcp.json` → `oracle`, `postgres` | for read checks; writes in this work were done with node-oracledb / container `psql`. `ORACLE_ALLOW_WRITE` was flipped to `true` on 2026-09-23 by user instruction — a `/mcp` reconnect is required for a running server to pick it up, and a server started before the literal password landed in `.mcp.json` answers every query with `ORA-01017` until reconnected. |

---

## 2. Files in this folder — and the PostgreSQL ones next door

| File | Role | Executed? |
|---|---|---|
| `oracle-aq-setup.sql` | **1** — queue `ACCOUNTING_EVENT_Q` (TxEventQ, JSON, single consumer FIFO, `max_retries 5`, `retry_delay 60`), `ACCOUNTING_EVENT_SEQ`, `ACCOUNTING_EVENT_LOG` (audit + replay source). Idempotent. Classic-AQ fallback commented. Rollback at end | **no** |
| `oracle-event-emit-package.sql` | **2** — `PKG_ACCOUNTING_EVENT.emit(...)`: builds the FIN API-FIN-020 JSON, logs, enqueues `ON_COMMIT` inside the business transaction, never raises | **no** |
| `oracle-event-emit-triggers.sql` | **3** — 7 `TRG_AE_*` triggers (LOAN_PAYMENT, CONTRACT, COMPLAINTS, COMPLAINT_DT, INVOICE_IMPORT, EXPENSE_TYPE_DT, TRANSFER_SAFE). INSERT → event; amount UPDATE → reversal + new; DELETE → reversal | **no** |
| `legacy-accounting-stop.sql` | **4** — disables 10 accounting triggers + 2 scheduler jobs; keep-list untouched; rollback companion inside | **YES — 2026-09-23 14:17:31.635 UTC** |
| `../seed-scripts/fin-seed-production.sql` | PostgreSQL, FIN v1 shape: 325 accounts, ORG/BRANCH, FY 2026 (12 OPEN months), 21 rules (6 active), MDL values. Idempotent | **YES** (twice, second run 0 rows) |
| `../seed-scripts/fin-seed-v2-delta.sql` | PostgreSQL, after v2 migrations: mapping rows (45), rule-line ORG/BRANCH tags, activates the 15 waiting rules | **no** — v2 schema absent |
| `../../seed-and-cutover-findings.md` | the full evidence: findings, decisions D-01..D-15, open questions | — |
| `../../oracle-aq-fin-integration-plan.md` | the architecture (queue → consumer → FIN), event contract, exactly-once, failure handling | — |

---

## 3. What has been done (with evidence)

| When (2026-09-23) | Action | Evidence |
|---|---|---|
| morning | Oracle investigation read-only: 29 triggers, 16 posting templates, 21 live branches, chart 328, org/branch, expense types; the May-2025 abandoned in-DB framework found still running | findings §2 |
| ~13:00 | v1 seed regenerated with accountant decisions (D-01 leaves 54/86/342 instead of parents 32/85/310, D-04, D-06, D-11 …) | findings §6 |
| ~13:05 | PostgreSQL reset: `DROP SCHEMA public CASCADE`, jar restarted, Flyway 31 migrations → v34 | findings §0 |
| ~13:10 | `fin-seed-production.sql` run twice — verification passed both times | findings §0 |
| **14:17:31.635 UTC** | **`legacy-accounting-stop.sql` executed** — 10 triggers DISABLED, `JOB_PROCESS_ACCOUNTING_QUEUE` + `LOAN_PAYMENT_DAILY_TR` DISABLED, keep-list 11/11 ENABLED, 0 automatic journal rows after marker | findings §0.2 |
| ~18:05 | FIN v2 delta discovered (delivered 18:02) → `fin-seed-v2-delta.sql` written; seed keys aligned (`EXPENSE_TYPE_CODE`) | findings §10 |
| ~19:00 | Oracle files 1–3 written; sign orientation corrected (D-14: negative is normal for FL 32/55/56/57/58/59/66 — 4 rules re-oriented in seed + live DB); OQ-14 closed (complaint mirrors are the only accounting for FL 50 → not skipped) | findings §11 |

### 3.1 First real execution of files 1–3 (2026-09-23 evening)

Until this point files 1–3 had only been written and read; nothing had ever been compiled or run.
Running them found four defects that review had not:

| # | What happened | Fix applied to the file |
|---|---|---|
| 1 | `DBMS_AQADM.ALTER_QUEUE` → **ORA-24218**, "feature ALTER_QUEUE not supported for transactional event queues". Because `CREATE_TRANSACTIONAL_EVENT_QUEUE` commits on its own, this left the queue **existing but never started** (enqueue NO / dequeue NO) — and the file's "skip if the queue exists" idempotence would have skipped straight past that forever. | `max_retries => 5` moved into the CREATE call; ALTER_QUEUE removed; the ELSE branch now *reasserts* shard count and start state instead of skipping. |
| 2 | `retry_delay` is **not settable at all** on a TxEventQ. `GET_QUEUE_PARAMETER` accepts only `SHARD_NUM`, `KEY_BASED_ENQUEUE`, `STICKY_DEQUEUE`; `RETRY_DELAY`/`RETRY_INTERVAL`/`MAX_RETRIES` each raise ORA-00904 "Unsupported param". | Documented in the file. The plan's 60 s spacing is now explicitly the **consumer's** responsibility. `retry_delay = 0` in `user_queues` is expected, not a defect. |
| 3 | The queue came up with **`SHARD_NUM = 5`** (the default). A TxEventQ orders messages only *within* a shard, so global FIFO — which plan §3.5 relies on, since an update is a reversal plus a new entry — was **not** actually in force. | `SET_QUEUE_PARAMETER(..., 'SHARD_NUM', 1)` on both the create and the repair path, plus a verification block that prints it. |
| 4 | `PKG_ACCOUNTING_EVENT` body → **PLS-00201: identifier 'DBMS_AQ' must be declared**. LOAN_SYS holds DBA but only as a *role* and has no direct object grants (`user_tab_privs`: no rows). Roles are live in an anonymous block and disabled inside a definer-rights stored unit — which is why every `DBMS_AQADM` call in file 1 worked while the package body would not compile. | New **section 0** in `oracle-aq-setup.sql`: `GRANT EXECUTE ON SYS.DBMS_AQ TO LOAN_SYS`. **NOT YET APPLIED — needs approval** (see below). |

**State on the test copy right now:**

| Object | State |
|---|---|
| `ACCOUNTING_EVENT_SEQ` | created |
| `ACCOUNTING_EVENT_LOG` | created, 0 rows |
| `ACCOUNTING_EVENT_Q` | created, **started** (enq YES / deq YES), `max_retries 5`, `SHARD_NUM 1` |
| `PKG_ACCOUNTING_EVENT` | spec **VALID**, body **INVALID** — blocked on defect 4 |
| `TRG_AE_*` (file 3) | **not run** |

Nothing is emitting and nothing is consuming, so an INVALID body is inert: no trigger references
it yet. The legacy triggers remain disabled from the 14:17 marker regardless.

**The one thing blocking progress:** `GRANT EXECUTE ON SYS.DBMS_AQ TO LOAN_SYS`. It is the
documented AQ requirement and the minimum that works — the alternative, making the package
invoker-rights (`AUTHID CURRENT_USER`), is worse here because the triggers that call it run as
whatever user saved the business row. On production a DBA issues the grant as SYS.

**Consequence in force:** since 14:17 UTC the legacy system records no accounting, and FIN receives nothing (no consumer). Every business row saved after the marker must be replayed (§5 step 6). `SELECT COUNT(*) FROM LOAN_PAYMENT WHERE CREATED_DATE > TIMESTAMP '2026-09-23 14:17:31.635 +00:00'` is the running size of that debt.

---

## 4. How to run what is here

```bash
# Oracle (as LOAN_SYS) — order matters.
# There is no sqlplus on the Mac host, but the erp-oracle container ships one
# (SQL*Plus 23.26.2.0.0 at /opt/oracle/product/26ai/dbhomeFree/bin/sqlplus).
# Use it — these files are written FOR sqlplus (SET ECHO ON, WHENEVER SQLERROR
# EXIT FAILURE, SHOW ERRORS, "/" terminators) and a hand-rolled splitter gets
# them wrong: see the warning below.
for f in oracle-aq-setup.sql oracle-event-emit-package.sql oracle-event-emit-triggers.sql; do
  docker cp "$f" erp-oracle:/tmp/"$f"
  docker exec erp-oracle sqlplus -S LOAN_SYS/LOAN_SYS@//localhost:1521/FREEPDB1 @/tmp/"$f" || break
done

# DO NOT drive these through node-oracledb by splitting the file yourself.
# That was tried 2026-09-23 and the splitter mis-parsed files 2 and 3: a package
# body and a trigger body both contain ";" on interior lines, so any rule that
# ends a statement at ";" cuts a PL/SQL block into fragments that will not
# compile. Only a lone "/" terminates those, which is exactly what sqlplus does
# and what an ad-hoc parser has to be told. node-oracledb is fine for single
# statements (that is how legacy-accounting-stop.sql ran — one ALTER per call),
# not for whole PL/SQL files.

# PostgreSQL (container psql; no local psql)
docker exec -i erp-backend-postgres psql -U postgres -d erp_db -v ON_ERROR_STOP=1 -f - < ../seed-scripts/fin-seed-production.sql
docker exec -i erp-backend-postgres psql -U postgres -d erp_db -v ON_ERROR_STOP=1 -f - < ../seed-scripts/fin-seed-v2-delta.sql   # only after v2 migrations
```

Verification queries are at the end of every file. Every file is re-runnable; every Oracle file has its rollback companion at the end.

**Production sequencing (differs from what happened on the test copy):** 1 → 2 → 3 with the legacy triggers still ENABLED (shadow), run the consumer, compare, **then** 4. On the test copy 4 ran first by explicit instruction; 1–3 can still run there any time — they do not touch the retired triggers.

---

## 5. Remaining — in order, with what each needs

| # | Step | Needs | How you know it is done |
|---|---|---|---|
| 1 | **Build FIN v2 backend** — `/FIN/v2/execute-backend` CORE → DATA-DOM → SVC-API → DOC → INT-C → INT-R → SEC-BE → ALIGN-BE | the executor must put the **OQ-13 data step** in the DATA-DOM migration (between `ADD COLUMN account_business_field_code` and `ADD CONSTRAINT CHK_FIN_RULE_LINE_DERIVATION_SPEC`): `UPDATE fin_rule_line SET account_business_field_code = account_derivation_value, account_derivation_value = NULL WHERE account_derivation_type_code = 'MAPPING' AND account_business_field_code IS NULL` — otherwise the migration fails on any DB carrying the v1 seed | `to_regclass('fin_account_mapping')` not null; `execution-state.json` phases COMPLETE |
| 2 | **Add `_REVERSED` mirror rules** (OQ-15) to `fin-seed-v2-delta.sql` — for each of the 21 rules a twin `<TYPE>_REVERSED` with DEBIT↔CREDIT swapped (+ MDL `ACCOUNTING_EVENT_TYPE` values). Needed for the rare opposite-sign row, every amount UPDATE, every DELETE | user's go (pending) | delta seed verification lists 42 rules |
| 3 | **Run `fin-seed-v2-delta.sql`** | step 1 | `NOTICE: verification passed — mappings 45+, tags 86+, active rules 21+` |
| 4 | **Run Oracle files 1 → 2 → 3** | nothing (test copy); on real production, do it before step 4 of the old order | `user_queues` shows `ACCOUNTING_EVENT_Q`; 7 `TRG_AE_*` VALID/ENABLED; first business save writes an `ACCOUNTING_EVENT_LOG` row with `ENQUEUED_FL = 1` |
| 5 | **Build the Event Consumer** — separate Spring Boot service: Oracle JMS dequeue (single thread, FIFO) → `POST /api/v1/fin/journal-entries/from-event` → consumer-side inbox in PostgreSQL; `409 FIN-409-DUPLICATE-EVENT` = success; needs a SEC user with exactly `PERM_FIN_JOURNAL_ENTRIES_CREATE` (1-hour JWT, session-backed — refresh on 401) | design: integration plan §3.1, §3.3, §3.4, §3.6 | an event enqueued in Oracle becomes a POSTED `FIN_JOURNAL_ENTRY` with that `event_reference` |
| 6 | **Replay 2026** from 1 Jan to now (and everything after the 14:17 UTC marker): regenerate events from the business tables in date order through `PKG_ACCOUNTING_EVENT.emit(..., p_source_action => 'REPLAY')`; opening balances into January (period OPEN); reconcile against `DAILY_RESTRICTIONS_*` keyed on legacy `CREATED_DATE` (D-15) | steps 3–5; the one unbalanced legacy master and the 6 unlinked complaint settlements are known mismatches | trial balance FIN = legacy at the marker |
| 7 | Close periods; retire stored balances (`SAFE`, `BANK_ACCOUNT` readers `GET_PREVIOUS_SAFE` / `GET_PERIVIOUS_SAFE`) | step 6 | — |

---

## 6. Decisions already taken (do not re-open without a reason)

| # | Decision | Where |
|---|---|---|
| D-01/02 | rules post to leaves `54 عملاء محليين`, `86 موردين محليين`, `342 المبيعات` — never the legacy parents 32/85/310 | seed §5 |
| D-04 | account nature: 22→DEBIT, 23→CREDIT, 31/NULL→root default; 175/263/234 corrected to DEBIT | seed §2 |
| D-06 | 8 `موظف تجريبي` test accounts inactive; 3 over-long codes excluded | seed §2 |
| D-07 | FL 8 / 130 have no rule but **are emitted** — a revival fails loudly at FIN | triggers file |
| D-08 | Retained Earnings = V34's `3200` | — |
| D-10 | one bank GL account (30) for every cheque, as legacy | mapping rows |
| D-11 | expense-type targets corrected: 81→150, 101→306, 422/424→26, 324/282→83 (rename 83, OQ-10) | delta seed §2b |
| D-14 | rule orientation follows the books; negative is the normal sign for FL 32/55/56/57/58/59/66 | seed §5, triggers `normal_sign` |
| D-15 | `docDate` = business date (legacy used `SYSDATE`) | package |
| OQ-14 | complaint mirrors in `EXPENSE_TYPE_DT` (FL 50) are emitted — they were the only accounting for complaint revenue | package `c_skip_complaint_mirrors := FALSE` |

## 7. Open questions still needing a human

| # | Question |
|---|---|
| OQ-07 | English account names (`name_en = name_ar` today) |
| OQ-10 | rename account `83 'حـساب /'` |
| OQ-13 | the DATA-DOM executor must include the data step (see §5 step 1) |
| OQ-15 | go for the `_REVERSED` mirror rules |
| F-14 | `COMPLAINT_DT_FL = 49 رسوم المستثمر` (119 rows in 2026) is accounted by neither system — intended? |

---

## 8. If something goes wrong

| Symptom | Do |
|---|---|
| Legacy users need the old journal back | run the ROLLBACK COMPANION in `legacy-accounting-stop.sql` (reverse order, ENABLE). Rows saved while it was off get no legacy journal retroactively |
| A business save fails after file 3 | it cannot be the emit (it swallows) — but check `ACCOUNTING_EVENT_LOG WHERE ENQUEUED_FL = 0` for the logged error; `ALTER TRIGGER TRG_AE_<table> DISABLE` isolates |
| Queue backs up / exception queue non-empty | `SELECT * FROM AQ$ACCOUNTING_EVENT_Q` (TxEventQ view name may be `AQ$<queue>`); replay from `ACCOUNTING_EVENT_LOG` by `EVENT_REFERENCE` |
| FIN seed must be re-applied on a fresh DB | migrations to v34 → `fin-seed-production.sql` → (v2) → `fin-seed-v2-delta.sql`; all idempotent |
