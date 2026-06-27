<!-- Source: MARK:PLAYWRIGHT / SUB:INT-FLOW -->


## PHASE:PLAYWRIGHT — Module Integration Flow (TP-SEC-4 — MANDATORY-P-4)

<!-- TC:TC-ORG-053:START -->
TC-ORG-053 — Module lifecycle: Create LegalEntity → verify in Search
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001, API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : LOV-ORG-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with full PERM_LEGAL_ENTITY permissions, clean state
When          : User creates LegalEntity: { nameAr: "كيان الاختبار", nameEn: "Test Entity", entityTypeId: "HEAD_OFFICE" }
Then          : HTTP 201 — new LegalEntity with code "LE-NNNNN" created
                User navigates to Search — searches by nameEn="Test Entity"
                New LegalEntity appears in results with correct data and isActiveFl=1

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-053:END -->

<!-- TC:TC-ORG-054:START -->
TC-ORG-054 — Module lifecycle: Update LegalEntity → verify change in Search
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-004, API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : LegalEntity "LE-NNNNN" created in TC-ORG-053
                User has PERM_LEGAL_ENTITY_UPDATE
When          : User opens "LE-NNNNN" → updates nameEn="Updated Entity"
                Clicks "حفظ"
Then          : HTTP 200 — nameEn updated
                legalEntityCode unchanged (RULE-ORG-011)
                User navigates to Search — record shows nameEn="Updated Entity"

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-054:END -->

<!-- TC:TC-ORG-055:START -->
TC-ORG-055 — Module lifecycle: Deactivate LegalEntity → verify removed from active Search
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-005, API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : LegalEntity "LE-NNNNN" from TC-ORG-053 — no active children
                User has PERM_LEGAL_ENTITY_DELETE
When          : User selects "LE-NNNNN" and clicks "تعطيل" → confirms deactivation
Then          : HTTP 200 — LegalEntity.isActiveFl=0
                User searches with default filter (isActiveFl=1)
                "LE-NNNNN" NOT visible in results (excluded from active list)
                Record still exists in DB (soft delete — not physical remove)

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-055:END -->

