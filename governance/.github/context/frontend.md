# Frontend Architecture

## Data Flow

```
Component → Facade → ApiService → Backend
```

| Layer | Responsibility |
|---|---|
| **Component** | UI rendering, `@Input`/`@Output` — ZERO business logic |
| **Facade** | State (signals), API orchestration, error handling |
| **ApiService** | HTTP via `BaseApiService` — no direct `HttpClient` in components |

## Implementation Order

```
Models → API Service → Facade → Routing → Components → Validate
```

## Key Contracts

| Concern | Rule |
|---|---|
| State | `signal()` private writable, `computed()` public readonly — never `BehaviorSubject` |
| Components | `standalone: true` + `ChangeDetectionStrategy.OnPush` always |
| Scoping | Facade and ApiService provided at page component level — never `providedIn: 'root'` |
| Create→Edit | `Location.replaceState()` after create — never `router.navigate()` |
| Numeric mapping | `??` not `\|\|` for numeric form-to-DTO values |
| Error display | `extractBackendErrorCode()` → `ErpErrorMapperService` — never raw HTTP errors |
| Theme | `ThemeService` owns all theme state — components never touch `document.body` |
| RTL | `LanguageService` exclusively owns RTL/LTR — no other service sets `document.dir` |

> For detailed rules and examples, read the relevant skill from `.github/skills/frontend/`.

---

## Navigation i18n Keys

> Single source of truth for `NAVIGATION.*` namespace in `en.json` / `ar.json`.
> Not defined in any skill file.

| Key | EN | AR |
|-----|----|----|
| `NOTIFICATION` | Notification | الإشعارات |
| `MESSAGE` | Message | الرسائل |
| `VIEW_ALL` | View all | عرض الكل |
| `MY_ACCOUNT` | My Account | حسابي |
| `SUPPORT` | Support | الدعم |
| `HELP` | Help | مساعدة |
| `PROFILE` | Profile | الملف الشخصي |
| `LOGOUT` | Logout | تسجيل الخروج |
| `SETTINGS` | Settings | الإعدادات |
