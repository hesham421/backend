```json
{
  "gate": "pass-1",
  "module": "FIN",
  "version": 2,
  "scores": { "unambiguous": 2, "verifiable": 2, "complete": 2, "consistent": 2, "singular": 3, "feasible": 2, "traceable": 2 },
  "extra_checks": [
    {"id": "ERP-1", "result": "PASS"},
    {"id": "ERP-2", "result": "PASS"},
    {"id": "ERP-3", "result": "PASS"},
    {"id": "ERP-4", "result": "PASS"},
    {"id": "ERP-6", "result": "PASS"}
  ],
  "analyze_confirmed": [
    {"finding": "C5.14 ac-measurable (AC-FIN-001…044, 046)", "confirmed": false, "note": "False positive caused by format. Every flagged AC is a v1 AC written inline ('Then the system rejects…'). AC-FIN-045, re-emitted in v2 with the 'Then   :' field form, is not flagged, which confirms the checker matches the field form and not the content. ADR-FIN-033 already records this. The rule-level fix is in the checker: accept an inline 'Then ' line. No SRS change is needed."},
    {"finding": "C5.15 crud-covered (12 rows: ENT-FIN-002, 003, 004, 006, 007, 009, 010, 011, 012, 013, 014, 016)", "confirmed": true, "note": "This corrects round 1, which said 16 rows over ENT-002..014. Each gap is a recorded decision or a recorded v1 gap. ENT-FIN-004: RULE-FIN-016 lock. ENT-FIN-010, 012, 014: add-only lines, no DELETE on any FIN screen. ENT-FIN-016: ADR-FIN-025. ENT-FIN-011 and 013 update: OPEN DEFECT at SCR-REQ-FIN-004/005 §B4. ENT-FIN-010's re-emitted Operations line still claims update and delete (see G3)."},
    {"finding": "C5.16 feature-unwanted (13 rows)", "confirmed": true, "note": "This corrects round 1, which said none of the flagged stories is v2. US-FIN-029 is a v2 story (REQ-FIN-070, REQ-FIN-071). Its unwanted paths exist but are traced to sibling stories: REQ-FIN-067, 068 and 069 under US-FIN-027 and 028. This is not a silent path. US-FIN-006…019 are v1 debt outside this delta."}
  ],
  "findings": [
    {"id": "G1", "severity": "MINOR", "artifact": "backend-execution-plan (CORE 'Event business fields'; API-FIN-020 steps 5 and 7; API-FIN-039, API-FIN-040; QR-FIN-052, 055, 058)", "line": null,
     "clause": "§3.5 P3 seam SRS→P3.1 / P4 pattern",
     "problem": "ADR-FIN-039 and the SRS field note on ENT-FIN-015.businessValue require trimming on save and on event read. No plan block says so: API-FIN-039/040 and QR-FIN-052/055 say only 'EXACT'. The same fields{} map feeds dimension resolution (API-FIN-020 step 7, QR-FIN-058), where no trim applies. As a result 'CHEQUE ' resolves a mapping but 'BR-07 ' fails RULE-FIN-027. A businessValue that is blank after trimming is still accepted on save for host-coded fields such as EXPENSE_TYPE_CODE (ADR-FIN-050, carried from the previous gate as MINOR). The operator's escalation fix did not take it up; it is carried here, not re-escalated.",
     "fix": "State one normalisation rule once, in CORE 'Event business fields': every fields[<code>] value and every submitted businessValue is trimmed before use. Blank after trimming counts as absent: on the event path it is FIN-422-MISSING-BUSINESS-FIELD (RULE-FIN-021 already says 'absent or blank'), and on save it is FIN-400 validation. Cite that sentence from API-FIN-020 steps 5 and 7, API-FIN-039, API-FIN-040, QR-FIN-052, QR-FIN-055 and QR-FIN-058. Applying the trim to dimension-tag reads extends ADR-FIN-039 and needs the adr flag.",
     "adr": true},
    {"id": "G2", "severity": "MINOR", "artifact": "registry-srs", "line": null,
     "clause": "C3.7 / C5.8 registry-agree (traceable)",
     "problem": "The tables and the Decisions list were fixed for ADR-FIN-051. Two summary lines were not. 'v2 delta: … MODIFIED ENT 1 · … RULE 1' should read ENT 3 (ENT-FIN-007, 008, 010) and RULE 2 (RULE-FIN-004, 008). The Event line still says '5 ADRs' and its MODIFIED list omits ENT-FIN-007, ENT-FIN-008 and RULE-FIN-008. The same file's tables and the SRS header (ADR 8) list them.",
     "fix": "Rewrite the delta count line to 'MODIFIED ENT 3 · REQ 1 · AC 1 · RULE 2 · SCR-REQ 2'. Rewrite the Event line to '8 ADRs; MODIFIED ENT-FIN-007, ENT-FIN-008, ENT-FIN-010, REQ-FIN-045, AC-FIN-045, RULE-FIN-004, RULE-FIN-008, SCR-REQ-FIN-003, SCR-REQ-FIN-006'.",
     "adr": false},
    {"id": "G3", "severity": "MINOR", "artifact": "srs (A3 ENT-FIN-010, re-emitted v2)", "line": null,
     "clause": "consistent / C5.15",
     "problem": "The v2-re-emitted ENT-FIN-010 record still lists Operations 'create…, read, update, delete'. A note under it concedes that no line update or delete exists (SCR-REQ-FIN-003 §B4, ADR-FIN-025). The record was re-emitted anyway, so leaving v1 wording that contradicts the as-built surface is avoidable.",
     "fix": "Narrow the Operations cell to 'create (with its rule and its dimension tags), read (embedded in the rule)'. Delete the 'v1's wording' sentence.",
     "adr": false},
    {"id": "G4", "severity": "MINOR", "artifact": "backend-execution-plan (PHASE 8 ALIGN-BE)", "line": null,
     "clause": "C7.12 xref-resolve / consistent",
     "problem": "The ALIGN block shows '✗ (C7.12: 1)' with a paragraph explaining it, then 'RESULT PASSED ✓ — 0 findings', which contradicts its own row. The current analyze report has no C7.12 finding: foreign ids now resolve against the target's folded state (commit b4ad6c6), so the ✗ and its paragraph are stale.",
     "fix": "Re-stamp the ALIGN block from the current analyze report (orchestrator, gov.py render). Mark C7.12 ✓ and delete 'The one ✗…' paragraph.",
     "adr": false},
    {"id": "G5", "severity": "MINOR", "artifact": "gate brief / platform-findings registry", "line": null,
     "clause": "§7 platform findings",
     "problem": "The brief's §2 load table rendered 17 empty rows ('`` () | ``'), and no platform-findings rows were supplied, so §7 could not be checked. ADR-FIN-029 says the fold's one-level carry limitation 'is reported with this run', but no platform-finding row reached the reviewer. If none was filed, a module worker has said 'not mine to settle' into the void.",
     "fix": "Platform (gov.py render/dispatch): fill §2 of the review-pass brief from the artifact list. Confirm that an OPEN platform-finding row exists for 'gov.py state carries only a re-emitted phase's direct children' (owner: gov.py state), citing ADR-FIN-029. File it if it is missing.",
     "adr": false},
    {"id": "G6", "severity": "MINOR", "artifact": "ADR stream (ADR-FIN-032…036, 043…053)", "line": null,
     "clause": "§7 decisions / traceable",
     "problem": "The gate-dialogue ADRs carry only a title ('the dialogue recorded the title alone'), with no Decision body. Two of them now state facts that later decisions overturned. ADR-FIN-045 says RULE-FIN-029 and an EXCLUDE constraint are in play; the revised ADR-FIN-040 withdrew both. ADR-FIN-046 demands guards; ADR-FIN-042 decided to record the races instead. Both remain RESOLVED-IN-DIALOGUE, not SUPERSEDED.",
     "fix": "Mark ADR-FIN-045 'SUPERSEDED by ADR-FIN-040 (revised)' and ADR-FIN-046 'SUPERSEDED by ADR-FIN-042'. Platform (dialogue ADR writer): record the finding text as the Decision body, not the title alone.",
     "adr": false}
  ],
  "adrs_reviewed": [
    {"id": "ADR-FIN-029", "status_ok": true, "note": "The full re-emission is consistent with the fold mechanics. The DBF-FIN-107/146 corrections appear in the Rule Registry and in the FIN-422-REMAINDER-MARKER row. The tool limitation it reports needs a platform-finding row (G5)."},
    {"id": "ADR-FIN-030", "status_ok": true, "note": "The advisor registry matches the constraints created in db-script BLOCK 5 and BLOCK 7, and the Concurrency lines of API-FIN-020 and API-FIN-039."},
    {"id": "ADR-FIN-031", "status_ok": true, "note": "fields.<CODE> is used in the plan and in every v2 event AC. QR-FIN-050 is the only join added."},
    {"id": "ADR-FIN-037", "status_ok": true, "note": "Fully applied: AC-FIN-047, 050…057, 061, 067, 068, 071…074 and 082 all use fields.<CODE>. The RULE-FIN-008 wording is re-emitted."},
    {"id": "ADR-FIN-038", "status_ok": true, "note": "The A3 preamble records the ENT-FIN-007/008 adaptation, so ERP-1 passes."},
    {"id": "ADR-FIN-039", "status_ok": true, "note": "The decision is sound and exact-code matching stays. Round 1's case-folding finding is withdrawn: it would reverse an ACCEPTED decision without evidence. The plan does not yet carry the trim (G1)."},
    {"id": "ADR-FIN-040", "status_ok": true, "note": "The withdrawal is fully applied: no RULE-FIN-029 and no overlap constraint. QR-FIN-059's Null handling now states the more-than-one-row case and cites the ADR, which closes ADR-FIN-049."},
    {"id": "ADR-FIN-041", "status_ok": true, "note": "All five PESSIMISTIC_WRITE guards are present, on API-FIN-014, 024, 025, 026 and 027."},
    {"id": "ADR-FIN-042", "status_ok": true, "note": "API-FIN-017, 002 and 003 each state their race and cite this ADR. API-FIN-003 is now correctly called 'update account'. The plan header lists ADR-FIN-040, 041 and 042. ADR-FIN-048 is closed."},
    {"id": "ADR-FIN-045", "status_ok": false, "note": "Should be SUPERSEDED by the revised ADR-FIN-040 (G6)."},
    {"id": "ADR-FIN-046", "status_ok": false, "note": "Should be SUPERSEDED by ADR-FIN-042 (G6)."}
  ],
  "verdict": "APPROVE"
}
```

DECISION: Round 1's G1 (case-folding of businessValue) is withdrawn. ADR-FIN-039 and ADR-FIN-026 deliberately choose exact-code matching. For PAYMENT_METHOD, RULE-FIN-024 lookup membership already rules out case variants. Host codes are codes. Reversing an ACCEPTED ADR needs evidence that nobody supplied.

DECISION: The remaining normalisation gap is G1 (MINOR). ADR-FIN-039's trim is not stated in any plan block. The trim applies to mapping reads but not to dimension reads of the same fields{} map. The blank-after-trim case (ADR-FIN-050) is still open. They are merged into one rule-level finding (P4). It is carried, not re-escalated, because the escalation that first saw it has been resolved by the operator.

DECISION: Round 1's analyze notes are corrected. C5.15 is 12 rows, not 16. US-FIN-029, flagged under C5.16, is a v2 story, and its unwanted paths are REQ-FIN-067…069 traced under US-FIN-027/028. Its C5.14 false-positive reading stands, with the format evidence recorded.

DECISION: Added G2, G3, G4, G5 and G6 as MINOR findings round 1 missed. G2 is the residual registry-srs count/event lag; the core of ADR-FIN-051 is fixed, so it is not a repeat. G3 is the stale ENT-FIN-010 Operations line. G4 is the stale ALIGN ✗ C7.12 that contradicts the RESULT line. G5 is the unrendered §2 brief and the unverified platform-finding row for the fold limitation. G6 is the dialogue ADRs with no Decision body and superseded status.

DECISION: Concurrency spot-check. The guarded endpoints PASS: API-FIN-011, 014, 019, 020, 021, 024…027 and 039. QR-FIN-053 last-writer-wins passes as a stated decision (ADR-FIN-043). API-FIN-017, API-FIN-002 and API-FIN-003 are FAIL, acknowledged. They are v1 races the operator decided to record (ADR-FIN-042), and each is now stated in its own block. They are not reopened as findings.

DECISION: The verdict is APPROVE. There is no CRITICAL, no MAJOR, no BLOCKED ADR, and no extra-check FAIL. Every score is at least 2. Every earlier escalation item (ADR-FIN-048…053) is verified closed in the artifacts, and all six findings are MINOR with a concrete fix.

<!-- CONVERGED -->