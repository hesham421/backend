I wrote the three files for `api-verify` FIN v2, but the script has never been run against the real API. No base URL or credentials were supplied, and the sandbox blocks network connections anyway. So the report states "generated, not executed" and has no pass/fail results yet. `gov.py analyze --scope stage:api-verify` is clean. The module-wide pass shows 0 critical, 0 major and 70 minor findings, and none of the minors mention these files. Nothing is committed.

**What I wrote** (in `governance-shared/analysis/modules/FIN/v2/`)
- **`api_verify/api-verify-fin.py`**: runs at the full tier because the manifest is present. It follows the manifest's dependency order and covers all 42 served endpoints, including the fiscal-years search that has no contract id. It asserts 30 of the manifest's rule → code → test-case rows as negative tests, checking the HTTP status, the error code and that the error message isn't empty. Rows it can't assert are listed with the reason. Checks that no rule and test case cover are recorded as observations and never affect the exit code. Cleanup runs at the end in reverse order and only deactivates rows the run created. It uses only the standard library.
- **`api_verify/api-verify-report-fin.md`**: I generated this with the script's own `--plan-only` mode, so the report and the script can't drift apart. A real run overwrites it with results.
- **`change-manifest.md`**: added `api-verify-script` and `api-verify-report` entries (`ADDED : all`, with the notes on their own lines so the tool doesn't read them as new ids).

**Choices you should know about.** I recorded these in the report as flagged documentation gaps (with the placeholders marked in the script), not as an ADR, because the brief allowed only these three files.
- **Unique codes get a run tag.** Account, dimension and fiscal-year codes and event references are unique across all rows, and FIN has no activate endpoint. Used exactly as in the docs, they would clash with existing data and with every rerun, so the script appends a run tag to them.
- **One event type per run.** A rule can exist only once per event type, ever, so `--event-type` must name a type that has no rule yet.
- **One API-docs example is invalid.** The example body for adding a rule line breaks two of FIN's own rules (RULE-FIN-019 and RULE-FIN-026), so the script builds each line shape from the field descriptions instead.
- **Some error codes may only appear in the per-field error list.** The docs say several codes come back that way, including the period-closed retry code, without saying what the top-level error code is then. The script accepts either location and records which one answered. If the retry code only shows up in the per-field list, the event consumer can't rely on it as documented.
- **Other gaps flagged:** the paging envelope doesn't document `content`; the error body has one `message`, not separate Arabic and English messages; the event endpoint's documented responses omit two codes the manifest expects there; and the login body isn't documented (the script uses placeholders).

**Testing done.** It compiles. The plan-only mode works, and a run against a closed port correctly stopped at the preflight check with exit code 2. A fuller in-process test, driving every suite against a fake responder, needed an approval this session doesn't have, so the happy-path suites have only been checked by reading them.

**To run it** against a Dev/Test backend:
```
python api-verify-fin.py --username <u> --password <p> --event-type <an event type with no rule yet>
```
The two permission-denied cases (TC-FIN-038 and TC-FIN-052) need a second, limited login via `--limited-username`/`--limited-password` or `--limited-token`. Year-end close (TC-FIN-036) needs `--with-year-end`, and it permanently hard-closes the run's own fiscal year.