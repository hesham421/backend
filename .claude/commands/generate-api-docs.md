# Generate Backend API Docs

```
Lives at   : backend/.claude/commands/generate-api-docs.md, so it
             auto-loads as a Claude Code slash command
Runs       : governance/governance-tools/api-doc-generator/generate.py
Writes to  : governance/modules/[MODULE]/api-docs/
```

(Re)generates a module's API documentation from the **running backend**, so
the frontend and the `api-verify` skill read documentation that matches the
implementation instead of a stale hand-written copy.

$ARGUMENTS = MODULE

The module code is the only input. Everything else — which springdoc group the
module is, the OpenAPI URL, the module's source root, the shared
`com.erp.common` root, the output directory — is auto-discovered from the
repository itself. Do NOT pass override flags unless discovery actually fails
and names the flag to pass.

## Preconditions

**Module validation:** confirm `governance/modules/$MODULE/` exists before
running anything. Resolve the code from `governance/modules-registry.json` or
`governance/modules/project-registry.md` — never invent one.

**Backend running:** the Spring Boot app must be up and `/v3/api-docs/<group>`
must answer for this module. The generator reads the real port from
`src/main/resources/application.properties` — never assume `8080`.

## Your Task

### STEP 1 — Review first, always

```bash
python3 governance/governance-tools/api-doc-generator/generate.py \
    --module $MODULE --function review
```

`review` writes nothing. Read its report before changing any file, and report:
endpoints added/removed/updated/unchanged, which shared `index.md` sections
changed, and any conflicts.

### STEP 2 — Then write, picking the mode from what review reported

- **Conflicts reported** (`unmanaged file already exists`) → STOP and show the
  list. Those are hand-written or hand-edited files; the tool refuses to
  clobber them. Ask before overwriting anything.
- **No `api-docs/` yet, or every file reported as added/unmanaged** →
  `--function generate` (full write, stamps every file with the
  AUTO-GENERATED marker).
- **Docs exist and carry the marker** → `--function update` (writes only what
  changed, deletes endpoint files for endpoints the backend no longer has,
  leaves everything else alone).

### STEP 3 — Report the outcome, concretely

Give the endpoint count, group count, error-code count, output path, and the
per-endpoint change list. If any section came out EMPTY, say so explicitly and
name the likely cause — never treat empty as normal:

| Symptom | Likely cause |
|---|---|
| No "Common Response Envelope" section | `ApiResponse<T>` wrapper not recognised in the OpenAPI schemas |
| "HTTP Status" column blank | `Status` → `HttpStatus` table not found in shared source |
| No "Required permission(s)" anywhere | `@PreAuthorize` constants not resolved from source |
| Every endpoint says "Authentication: Not determined" | backend declares no `SecurityScheme` in `OpenApiConfig` — a known backend gap, not a generator failure |

## Constraints (NON-NEGOTIABLE)

- **NEVER hand-edit a generated file under `api-docs/`.** It is regenerated
  output; a manual edit is destroyed on the next run AND turns the file into a
  conflict the tool then refuses to manage. If the docs are wrong, the backend
  is wrong or under-annotated — fix it there and regenerate.
- **NEVER pass `--openapi` / `--source` / `--common-source` / `--output` to
  "make it work."** If discovery fails it names the exact flag and why; report
  that instead of working around it.
- **NEVER invent content for a section the generator left out.** Absent means
  "not discoverable from the implemented backend" — that is information.
- **NEVER fall back to a saved or older OpenAPI JSON.** If the generator
  reports HTTP 500 from `/v3/api-docs/<group>`, that is a backend/springdoc
  fault, not a documentation fault: report it with the response body and stop.
  Docs that look current but aren't are worse than no docs.

## Notes

- Output always lands in `governance/modules/[MODULE]/api-docs/`
  (`index.md` + `endpoints/<group-slug>.md`). The path derives from the tool's
  own location, so the command works from any working directory.
- `review` is safe to run any time, including in CI, to answer "have the API
  docs drifted from the backend?" without touching a file.
- Consumers of this output: the frontend repo's own independent
  `modules/[MODULE]/api-docs/` copy, and the `api-verify` skill — see
  `governance/api-verify-config.md` §1, which lists api-docs as its
  **mandatory** input and says to regenerate rather than trust a stale copy.
