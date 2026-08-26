# Frontend Architecture

> Stack: React + TypeScript, TanStack Query v5, React Hook Form + Zod, React
> Router v7. This document reflects the skill pack at
> `frontend/governance/.github/skills/frontend/` — read the relevant skill
> there for detailed rules and examples; this is the summary.

## Data Flow

```
Component (List / Entry page) → TanStack Query hooks → API client → Backend
                                → React Hook Form + Zod (forms)
                                → React Context (cross-cutting client state)
                                → URL search params (list/filter state)
```

| Layer | Responsibility | Skill |
|---|---|---|
| **Component** | UI rendering only — columns, list page, entry page. ZERO business logic | `create-components` |
| **API client** | Shared fetch client (auth injection, envelope unwrapping, error normalization, 401 refresh) + a feature's typed API module — no direct `fetch` calls in components | `create-api-client` |
| **TanStack Query hooks** | Server-state reads/mutations: query key factory, list/detail/usage, optimistic rollback | `create-queries` |
| **React Hook Form + Zod** | Form state: shared `<Entity>Form`, submit handling, server-error mapping, dirty tracking, unsaved-changes guarding | `create-forms` |
| **React Context** | Cross-cutting client state only — `LanguageContext` (locale/direction/`t()`), `AuthContext` (session/grants). Nothing that belongs in Query, Form, or URL state | `create-app-state` |
| **URL search params** | List/filter/pagination state | (part of `create-components` / list pages) |
| **Auth/session** | In-memory token store, httpOnly refresh-cookie flow, startup bootstrap, single-flight 401 refresh, multi-tab sync | `create-auth-session` |
| **Error architecture** | Closed `ApiError` taxonomy, `normalizeError`, `mapBackendError`, route error elements, layered error boundaries | `create-error-handling` |
| **Routing** | Canonical path constants, static code-first route tree, lazy loading, auth/permission guards, error/404 routes | `create-routing` |

## No duplicated sources of truth

Per `enforce-state-management`: server state lives in TanStack Query, form
state in React Hook Form, list state in the URL, session in one query, and UI
state in React Context — never two of these for the same value.

## Key Contracts

| Concern | Rule |
|---|---|
| HTTP | All requests go through the shared API client from `create-api-client` — never a raw `fetch`/`axios` call in a component, hook, or Context |
| Errors | `normalizeError` / `mapBackendError` → never a raw HTTP error surfaced to a component |
| Permissions | Triple-enforced: route guards, UI gating, and a programmatic check before the action itself — see `enforce-permissions` |
| Destructive actions | Permission check, then usage/eligibility check, then the confirm dialog — never the dialog first — see `create-confirm-actions` |
| i18n / RTL | `LanguageContext` is the sole owner of locale, direction, and `t()` |
| Precedence | When general React guidance conflicts with a project rule, the project rule wins — see `erp-priority-override` |

> For detailed rules, checks, and examples, read the relevant skill from
> `.github/skills/frontend/` in the frontend repo.

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
