# Added G3, a gap between P3.1 and P3.2. The record pages that ADR-FIN-065 introduces hydrate through `<entity>Pk EQUALS` filters: `dimensionPk` on API-FIN-005, `eventTypeRulePk` on API-FIN-009, `recurringTemplatePk` on API-FIN-012, `allocationRulePk` on API-FIN-015, and `fiscalYearPk` on the fiscal-year search. None of those filters appears on the Request lines of the backend plan. ADR-FIN-065 cites the api-docs' "Supported fields (EQUALS, IN)", and api-verify-script's `search_by` depends on them too. So the as-built surface supports the filters and the backend plan is stale. MINOR, P3.1 to fix.

Lane   : review-pass · round 2 · claude:opus
Stage  : P3.2        Module: FIN        Version: v2
Written: 2026-09-24T03:42:07+00:00


