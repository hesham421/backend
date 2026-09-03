<!-- Source: PHASE:DATA-DOM / SUB:DATA-DOM-IDENTITY -->
<!-- Context: see DATA-DOM-HEADER.md for phase-level strategy, registry table, and intro -->

### ENTITY-SEC-001 — UserAccount (SHARED — owner SEC)
  DB Table: SEC_USER_ACCOUNT | PK: ID | Sequence: SEQ_SEC_USER_ACCOUNT | DBS-SEC-001
  BUSINESS CODE: NONE (username is the natural key).
  SHARED: consumed SOFT-READ by FILE (XM-FILE-001) & NOTIF (XM-NOTIF-001). Owner-only writes.
  FIELDS (FIELD-0001..0011 → DBF-0001..0011):
   FIELD-0001 userAccountPk ID DBF-0001 BIGINT PK SEQ_SEC_USER_ACCOUNT | المعرف / ID
   FIELD-0002 username USERNAME DBF-0002 VARCHAR(100) UNIQUE UQ_..._USERNAME Read-only after create | اسم المستخدم / Username
   FIELD-0003 passwordHash PASSWORD_HASH DBF-0003 VARCHAR(255) System — never in any DTO (RULE-SEC-004) | — / —
   FIELD-0004 email EMAIL DBF-0004 VARCHAR(255) UNIQUE UQ_..._EMAIL | البريد / Email
   FIELD-0005 phone PHONE DBF-0005 VARCHAR(30) nullable | الهاتف / Phone
   FIELD-0006 fullName FULL_NAME DBF-0006 VARCHAR(200) NOT NULL | الاسم الكامل / Full Name
   FIELD-0007 preferredLangId PREFERRED_LANG_ID DBF-0007 VARCHAR(10) LOV-SEC-001 code | اللغة المفضّلة / Preferred Language
   FIELD-0008 userStatusId USER_STATUS_ID DBF-0008 VARCHAR(50) LOV-SEC-002 code (lifecycle A6) | حالة الحساب / Status
   FIELD-0009 failedLoginCount FAILED_LOGIN_COUNT DBF-0009 SMALLINT System (RULE-SEC-005) | محاولات فاشلة / Failed Logins
   FIELD-0010 lockedUntil LOCKED_UNTIL DBF-0010 TIMESTAMP nullable | مقفول حتى / Locked Until
   FIELD-0011 isActiveFl IS_ACTIVE_FL DBF-0011 SMALLINT DEFAULT 1 · CHK IN(0,1) | نشط / Active
  DTO: CreateRequest{username,email,phone,fullName,preferredLangId} (password via activation/reset flow, not create body);
       UpdateRequest{email,phone,fullName,preferredLangId,userStatusId,isActiveFl} (username immutable; passwordHash never);
       ResponseDTO excludes passwordHash always.
  STATE MACHINE (userStatusId — LOV-SEC-002): PENDING_ACTIVATION → ACTIVE ⇄ INACTIVE (RULE-SEC-012 reactivation). Initial: PENDING_ACTIVATION.
  DOMAIN RULES: RULE-SEC-001 (unique username), RULE-SEC-002 (required fields), RULE-SEC-003 (password complexity),
   RULE-SEC-004 (store hashed only), RULE-SEC-005 (lock after N failed), RULE-SEC-009 (block login when status≠ACTIVE),
   RULE-SEC-012 (deactivation allowed, no cascade, history retained, reactivation permitted). Full text in Error Catalog + SVC+API.
  XM: SHARED-owner; no outbound XM. QR: QR-SEC-0001..0006.
