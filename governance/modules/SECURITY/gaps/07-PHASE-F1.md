# PHASE 7 — F1 (Screens)
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 7.1

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase F1** of PLAN-SEC-002 for the Security module,
in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-6-INT-R.md`. If missing, or DOC ✓/INT-C ✓ do
not both hold — STOP (CONTRACT ALIGNMENT GATE per plan Section 12: "If
either fails → F1, F2, F3 are BLOCKED").

**Source of truth: `execution-plan-SEC-gaps.md` Section 7.1** (F1 —
Screen Specifications) in full, cross-checked against
`srs-security-gaps.md` Section 3.1 (exact sidebar/quick-access placement
in Arabic UI) if that file is present in the repo.

TASK — Angular routing/screen shells ONLY (no business logic — that is
F2/F3):
1. SCR-SEC-006 (ملفات المستخدمين / نطاق البيانات) — NEW sidebar item
   under the "الأمان" group (after "إدارة الصفحات"), NEW quick-access
   card, PATTERN-1 (Search + Entry) — ONE Angular route module, ONE
   lazy-loaded module (per CORE-9 in the plan/governance core).
2. SCR-SEC-002 extension — add a "نطاق الفروع" (Branch Scope) SUB-TAB
   inside the EXISTING Role Management screen/component — do NOT create
   a new route, new module, or new sidebar entry for it (CORE-9: no
   sub-screen gets its own SCR-ID/route).
3. SCR-SEC-008 (Sign Up) — new PUBLIC route, reachable from the "ليس
   لديك حساب؟" link already present on the login screen (wire the link,
   do not change the login screen otherwise).
4. SCR-SEC-009 (Forgot/Reset Password) — new PUBLIC route (2-step:
   request view, then reset view from a token in the URL), reachable
   from the "نسيت كلمة المرور؟" link already present on the login screen.
5. Route guards: SCR-SEC-006 and the Role sub-tab require
   authentication (guard wiring only — actual permission checks are
   Phase SEC); SCR-SEC-008/009 are public, no guard.

DEFINITION OF DONE:
- [ ] 3 new routes (SCR-SEC-006, 008, 009) + 1 sub-tab (SCR-SEC-002 ext)
- [ ] No new route/module/sidebar-item for the Branch Scope sub-tab
- [ ] Both login-screen links wired to their new destinations
- [ ] Screens render (empty/placeholder content acceptable — F2 fills state)

Write `HANDOFF-PHASE-7-F1.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 7 (F1) — PLAN-SEC-002
Routes/components created: [list + paths]
Sidebar/quick-access changes: [describe]
Confirmed Branch Scope sub-tab has NO separate route/module: [yes/no]
Login screen links wired: [yes/no, both]
Ready for Phase 8 (F2)? [yes/no]
```
