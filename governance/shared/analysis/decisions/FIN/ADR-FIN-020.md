# ADR-FIN-020 — v2 stories use v1's role names wherever the actor is the same
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0.5        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T10:01:54+00:00
Dialogue-key: f69236979a73
traces      : —

## Decision
Round 2 added three roles that don't exist in v1: "مسؤول إعداد FIN", "مستخدم التقارير المالية" and "مراجع التشغيل المتوازي". The v1 stories in `_state/current-prd.md` already name the actors for this kind of work:
- **Finance administrator (مسؤول المالية):** configures event-type rules and dimensions.
- **Financial controller (المراقب المالي):** owns the trial balance and dimension reports.

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0.5-round3.response3.md
