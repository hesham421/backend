# Superseded run - not part of module test archive

All 10 generated tests failed on the login step: the generated Python scripts
read `response.json()["accessToken"]` directly, but the real API wraps every
payload in an ApiResponse envelope (`{success, message, data: {...}, timestamp}`).
The code_summary.yaml given to TestSprite for this attempt didn't document that
envelope, so every test's auth helper broke before exercising any real
Organization endpoint behavior. Fixed by adding a `global_conventions` note to
`testsprite_tests/tmp/code_summary.yaml` and re-running (see
`governance/testsprite/runs/2026-08-29b-backend/` for the corrected run).

Kept here for history only - do not treat as ORG module coverage.
