# Platform — audit-column widths and `GlobalExceptionHandler` localization

Two issues surfaced during FIN's ALIGN-BE work on 2026-09-12 that are **not FIN's to own**.
Both sit in `com.erp.common.*` / `src/main/resources/db/migration/` and affect SEC, MDL, FIN,
CU, NOTIF and FILE identically. FIN's `execution-state.json` carries them as `api_doc_gaps[]`
entries so they are not lost from the module record, but neither can be closed by a module
session — hence this note.

Every number, string and line reference below was re-derived by opening the file on
2026-09-12. Nothing is carried from a prior document. Where a claim is quoted, the quote was
read at the cited line; where a count is given, the command that produced it is named.

Companion records:
- `governance/modules/FIN/execution-state.json` → `api_doc_gaps[]`, the entries whose
  `endpoint` begins `DB Alignment Manifest (§4)` and `common/web/GlobalExceptionHandler`.
- The `MANIFEST (§4)`, `ERROR ENVELOPE` and `PLATFORM I18N` lines of the ALIGN self-check in
  `governance/modules/FIN/packages/backend-execution/_SECTIONS.md` (mirrored verbatim in
  `governance/modules/FIN/P3_1/backend-execution-plan-fin.md`).

---

## Part 1 — `created_by` / `updated_by` are two different widths across the platform

### The split, as built

Counted by grepping `(created_by|updated_by)[[:space:]]+VARCHAR` across
`src/main/resources/db/migration/` and then discarding the eight `SEC_*` tables created by
`V2__sec_security_schema.sql`, every one of which
`V14__drop_legacy_security_schema.sql:9-19` drops. What remains is 22 live tables — exactly
the 22 classes returned by `grep -rl "extends AuditableEntity" src/main/java/com/erp/`.

**16 live tables at `VARCHAR(100)`** (SEC 5 · MDL 2 · FIN 9):

| Table | Migration | `created_by` : `updated_by` |
|---|---|---|
| `SEC_USER` | `V16__sec_schema.sql` | 52 : 54 |
| `SEC_ROLE` | `V16__sec_schema.sql` | 66 : 68 |
| `SEC_MODULE_REG` | `V16__sec_schema.sql` | 78 : 80 |
| `SEC_SCREEN_REG` | `V16__sec_schema.sql` | 113 : 115 |
| `SEC_ACTION_REG` | `V16__sec_schema.sql` | 127 : 129 |
| `MDL_LOOKUP_TYPE` | `V18__mdl_sequences.sql` | 37 : 39 |
| `MDL_LOOKUP_VALUE` | `V18__mdl_sequences.sql` | 54 : 56 |
| `FIN_DIMENSION` | `V22__fin_schema.sql` | 52 : 54 |
| `FIN_ACCOUNT` | `V22__fin_schema.sql` | 68 : 70 |
| `FIN_FISCAL_YEAR` | `V22__fin_schema.sql` | 81 : 83 |
| `FIN_EVENT_TYPE_RULE` | `V22__fin_schema.sql` | 93 : 95 |
| `FIN_RECURRING_TEMPLATE` | `V22__fin_schema.sql` | 109 : 111 |
| `FIN_ALLOCATION_RULE` | `V22__fin_schema.sql` | 121 : 123 |
| `FIN_DIMENSION_VALUE` | `V22__fin_schema.sql` | 137 : 139 |
| `FIN_FISCAL_PERIOD` | `V22__fin_schema.sql` | 154 : 156 |
| `FIN_JOURNAL_ENTRY` | `V22__fin_schema.sql` | 174 : 176 |

**6 live tables at `VARCHAR(255)`** (CU 1 · NOTIF 3 · FILE 2):

| Table | Migration | `CREATED_BY` : `UPDATED_BY` |
|---|---|---|
| `CU_APP_CONFIGURATION` | `V1__cu_app_configuration_schema.sql` | 25 : 27 |
| `NOTIF_TEMPLATE` | `V6__notif_schema.sql` | 30 : 32 |
| `NOTIF_CHANNEL_CONFIG` | `V6__notif_schema.sql` | 41 : 43 |
| `NOTIF_LOG` | `V6__notif_schema.sql` | 62 : 64 |
| `FILE_CATEGORY` | `V8__file_schema.sql` | 26 : 28 |
| `FILE_DOCUMENT` | `V8__file_schema.sql` | 47 : 49 |

The 100-width group is `NOT NULL`; the 255-width group is nullable. The 255 group is also the
older one (V1/V6/V8, the Legacy Path modules), the 100 group the newer (V16/V18/V22).

### What was changed on 2026-09-12, and what was not

`src/main/java/com/erp/common/domain/AuditableEntity.java` previously declared both columns
`length = 255`, which contradicted 16 of the 22 physical tables. It was narrowed to match the
**narrowest** live width:

- `AuditableEntity.java:26` — `@Column(name = "CREATED_BY", updatable = false, length = 100)`
- `AuditableEntity.java:32` — `@Column(name = "UPDATED_BY", length = 100)`

with an in-file comment recording the reasoning so it is not "tidied" back to 255.

**No migration was written and the physical schema was not touched.** The split above is
exactly as it was before the change. This is a declaration fix only.

### Why the change is runtime-inert

`src/main/resources/application.properties:21` reads:

```
spring.jpa.hibernate.ddl-auto=none
```

immediately under the comment at line 20, *"Schema is owned by Flyway, so Hibernate never
generates DDL (ddl-auto=none)."* With `none`, Hibernate neither generates DDL from
`@Column(length = ...)` nor validates it against the live column, and it does not truncate
values on write. The annotation is therefore documentation-of-intent at runtime; changing 255
to 100 cannot alter observable behaviour on any environment. (It *would* start mattering the
day anyone switched to `validate` — at which point the 6 wide tables would become the
mismatch, not the 16 narrow ones.)

### Why 100 is the true upper bound anyway

The value written into these columns is chased from the listener down to the schema:

1. `com/erp/common/audit/AuditEntityListener.java` — `@PrePersist`/`@PreUpdate` set both
   columns from `SecurityContextHelper.getCurrentUsername()`.
2. `com/erp/common/util/SecurityContextHelper.java` — returns
   `authentication.getName()`, or the literal `"system"` when there is no authenticated
   principal.
3. `com/erp/sec/security/JwtAuthenticationFilter.java:79` takes `claims.getSubject()` as
   `username`, and `:85` requires `userRepository.findByUsername(username)` to resolve before
   `:99`/`:102` construct the `UsernamePasswordAuthenticationToken` with that same `username`
   as its principal. So `getName()` is never an arbitrary token claim — it is always a row's
   `SEC_USER.username`.
4. `src/main/resources/db/migration/V16__sec_schema.sql:44` —
   `username       VARCHAR(100)  NOT NULL`.

So every value is either a `SEC_USER.username` (≤ 100 by schema) or the 6-character literal
`"system"`. The 255-width columns are over-provisioned; they are not carrying wider data.

### What a human still owes

Nothing is broken and nothing is urgent. The open question is whether to reconcile the
physical schema, and it is a platform call because it spans three modules that no FIN, SEC or
MDL session owns:

- **Option A — leave it.** Runtime-correct today, costs nothing, but `ddl-auto=validate` stays
  unavailable and the inconsistency will keep being re-discovered by every alignment sweep.
- **Option B — one forward migration** narrowing the 12 CU/NOTIF/FILE columns (6 tables ×
  `created_by` + `updated_by`) to `VARCHAR(100)`. Safe by the chain above (no stored value can exceed 100), but it rewrites
  columns in three modules' tables and needs whoever owns CU/NOTIF/FILE to agree.

Do **not** resolve it by widening `AuditableEntity` back to 255 — that re-breaks the 16-table
majority to accommodate 6 over-provisioned columns.

---

## Part 2 — `GlobalExceptionHandler` localizes two codes out of five

All line references are `src/main/java/com/erp/common/web/GlobalExceptionHandler.java`, read
at its state on 2026-09-12.

### Current state, handler by handler

| Handler | Lines | Code on the wire | Message source | Bundle entry (en / ar) |
|---|---|---|---|---|
| `LocalizedException` | 32-46 | the exception's own `errorCode` | **`resolveMessage(...)` → `MessageSource`** | per-code, e.g. all 37 `FIN-*` keys |
| `MethodArgumentNotValidException` | 54-68 | `"VALIDATION_ERROR"` (literal) | hardcoded `"Validation failed"` | **none / none** |
| `HttpMessageNotReadableException` | 70-78 | `"VALIDATION_ERROR"` (literal) | hardcoded `"The request body is malformed or does not match the expected structure"` | **none / none** |
| `DataIntegrityViolationException` | 80-88 | `"DATA_INTEGRITY_VIOLATION"` (literal) | hardcoded `"The request could not be completed because it violates a data constraint"` | **none / none** |
| `AccessDeniedException` | 90-96 | `CommonErrorCodes.ACCESS_DENIED` | **`resolveMessage(...)` → `MessageSource`** | `messages.properties:16` / `messages_ar.properties:13` |
| `Exception` (catch-all) | 99-105 | `"INTERNAL_ERROR"` (literal) | hardcoded `"An unexpected error occurred"` | **present in both, and unused** — `messages.properties:15` / `messages_ar.properties:12` |

`resolveMessage(String, Object[])` is the private helper at `:109-115`: it reads
`LocaleContextHolder.getLocale()`, calls `messageSource.getMessage(...)`, and on
`NoSuchMessageException` logs a warning and falls back to returning the code itself.

The practical consequence: an Arabic-locale caller gets Arabic for any `LocalizedException`
and, since 2026-09-12, for a 403 — but English for every 400 validation failure, every 409
constraint violation and every 500.

### What changed on 2026-09-12

Only the `AccessDeniedException` handler. It previously read
`.code("ACCESS_DENIED")` / `.message("You do not have permission to perform this operation")`
and now reads `.code(CommonErrorCodes.ACCESS_DENIED)` /
`.message(resolveMessage(CommonErrorCodes.ACCESS_DENIED, null))`. Supporting changes:
`CommonErrorCodes.java:11` gained the `ACCESS_DENIED` constant, and the key was added to both
bundles.

The English bundle text is **byte-identical** to the string it replaced, so no English-facing
response moved; only Arabic callers see a difference. That was deliberate, and it is the
property that made the change safe to land without asking anyone.

The **wire code is unchanged**: FIN's 403 is still the platform `ACCESS_DENIED` envelope, and
`FIN-403-FORBIDDEN` still reaches no caller — it exists in no `FinErrorCodes` constant and in
neither bundle. Routing `AccessDeniedException` through `LocalizedException` as a module code
would change every module's 403 body and remains an untaken platform decision, recorded as the
`ERROR ENVELOPE` finding in FIN's ALIGN self-check.

### The one decision a human owes: the `INTERNAL_ERROR` wording conflict

This is the only item here that cannot be fixed mechanically.

- The handler hardcodes: `An unexpected error occurred`
- `messages.properties:15` says: `An unexpected error occurred. Please try again later.`
- `messages_ar.properties:12` says: `حدث خطأ غير متوقع. يرجى المحاولة لاحقاً.`

Both bundles already carry the key; nothing resolves it. So `INTERNAL_ERROR` is *ready* to be
localized the same way `ACCESS_DENIED` just was — except that the two English strings are not
the same. Routing it through `resolveMessage` would silently change the 500 body for every
existing English caller, which is precisely what the `ACCESS_DENIED` change avoided.

**Someone must choose which English wording is canonical** before the one-line change is made:

- (i) adopt the bundle text (`... Please try again later.`) — English 500 bodies change; or
- (ii) edit the bundle down to `An unexpected error occurred` and adjust the Arabic to match —
  English unchanged, Arabic gains localization with no English drift, mirroring exactly how
  `ACCESS_DENIED` was handled.

Option (ii) is the consistent precedent, but it is a product-copy call, not an engineering one,
so it is left open rather than assumed.

### The mechanical remainder

`VALIDATION_ERROR` and `DATA_INTEGRITY_VIOLATION` have **no bundle entry in either language** —
`grep '^VALIDATION_ERROR=' src/main/resources/i18n/messages*.properties` and the same for
`DATA_INTEGRITY_VIOLATION` return nothing. Localizing them needs keys minted first, and
`VALIDATION_ERROR` carries a second question: two different handlers (`:54-68` and `:70-78`)
emit two different English messages under that one code, so a decision is needed on whether
they share a key or get separated (e.g. a distinct `MALFORMED_REQUEST_BODY`).

Separately and cosmetically: `CommonErrorCodes` already declares `VALIDATION_ERROR` (`:9`) and
`INTERNAL_ERROR` (`:10`), but these three handlers use string literals rather than the
constants. Worth folding into whichever change lands.

### Scope note

None of Part 2 is FIN-specific. It affects SEC, MDL, FIN, CU, NOTIF and FILE identically,
because `GlobalExceptionHandler` is a single `@RestControllerAdvice` in `com.erp.common.web`.
No module-level ALIGN session can close it.
