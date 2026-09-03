<!-- Source: PHASE:DATA-DOM / SUB:DATA-DOM-TOKENS -->
<!-- Context: see DATA-DOM-HEADER.md for phase-level strategy, registry table, and intro -->

### ENTITY-SEC-005 — RefreshToken Table SEC_REFRESH_TOKEN | SEQ_SEC_REFRESH_TOKEN. FIELDS 0030-0034 (token UNIQUE+hashed, expiresAt, revokedFl, userAccountFk→SEC_USER_ACCOUNT).
  Rules: RULE-SEC-006 (rotate on refresh; access TTL ⚠15m, refresh ⚠7d). QR-SEC-0019.
### ENTITY-SEC-006 — PasswordResetToken Table SEC_PASSWORD_RESET_TOKEN | SEQ_SEC_PASSWORD_RESET_TOKEN. FIELDS 0035-0039 (token UNIQUE+hashed, expiresAt, usedFl, userAccountFk).
  Rules: RULE-SEC-007 (single active, single-use, TTL ⚠60m). QR-SEC-0020.
### ENTITY-SEC-007 — AccountActivationToken Table SEC_ACCOUNT_ACTIVATION_TOKEN | SEQ_SEC_ACCOUNT_ACTIVATION_TOKEN. FIELDS 0040-0044 (token UNIQUE+hashed, expiresAt, usedFl, userAccountFk).
  Rules: RULE-SEC-008 (single active, single-use, TTL ⚠24h). QR-SEC-0021. Internal entities — no nameAr/nameEn, no screen.
