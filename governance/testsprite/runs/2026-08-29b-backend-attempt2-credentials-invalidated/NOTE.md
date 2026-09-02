# Superseded run - not part of module test archive

All 5 tests failed at the login step. The code_summary envelope fix from
attempt 1 worked correctly (scripts here properly read `resp.json()["data"]["accessToken"]`),
but backend logs show the admin account's password was changed mid-run:

  20:52:59 AuthService - Password reset completed for username: admin

This happened via POST /api/auth/reset-password, which none of these
generated test scripts call - some other concurrent process/session hit
that endpoint against this same running backend instance while this
TestSprite run was in flight, invalidating the hardcoded admin/admin
credentials TestSprite authenticates with. Backend logs also show 3
unexplained Spring context restarts earlier in this session (20:16, 20:24,
20:27) and this repo saw drive-by git commits ("aa" x2) at 20:40 that this
session didn't make - all pointing at concurrent activity on this backend/repo
outside this TestSprite run's control.

Kept here for history only - do not treat as ORG module coverage. Re-run
once the environment is confirmed idle and admin/admin (or the current
actual password) is verified working.
