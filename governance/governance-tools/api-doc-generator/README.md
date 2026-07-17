# api-doc-generator

Shared ERP tool that generates frontend-ready API documentation directly from
an implemented Spring Boot module. It is not built for any specific module —
it works identically for ORG, Security, Master Data, Finance, and any future
module that follows this platform's existing conventions (Maven reactor,
`GroupedOpenApi` per module, shared `erp-common-utils`), with no generator
change required to support a new one. There is no governance coupling, no
execution-plan parsing, no drift/contract verification here. **The
implemented backend is the only source of truth.**

## Purpose

Frontend developers need accurate, up-to-date API documentation. Hand-written
docs drift from the real implementation the moment either side changes. This
tool regenerates documentation from the actual running/compiled backend every
time, so it can never be stale — if it's wrong, the backend is wrong (or
under-annotated), not the doc generator.

## Quick start

```bash
python3 generate.py --module ORG --function generate
python3 generate.py --module ORG --function update
python3 generate.py --module ORG --function review
```

That's it in normal use — see "Automatic discovery" below for what these two
flags actually resolve to and why nothing else is usually needed.

## Architecture

```
generate.py              CLI entrypoint. Parses --module/--function (+ rare
                         explicit overrides), calls discovery.py, then calls
                         generator.run(). Owns no pipeline logic itself.
discovery.py             Resolves --module into a RepositoryContext by reading
                         real repository artifacts (Maven reactor, springdoc
                         group config, WORKSPACE.md's sibling-repo layout) --
                         never a per-module lookup table. See "Automatic
                         discovery" below. RepositoryContext is the ONLY
                         object carrying repository-shaped knowledge
                         (locations, which module, which shared resources)
                         across the discovery -> pipeline boundary.
generator.py             The pipeline: extract -> render -> sync.
                         build_document(context) is the only place that
                         RepositoryContext gets unpacked; run(context, mode)
                         and build_document() are plain functions, importable
                         by generate.py or any other caller (tests, a future
                         batch-all-modules script, ...). Every individual
                         extractor still takes its own narrow, explicit input
                         (a Path, a dict, ...), never the context object
                         itself — extractors have no dependency on
                         discovery.py and no way to perform repository
                         discovery themselves, by construction.
models/
  api_doc_model.py       Dataclasses shared by every extractor and renderer.
                         Every field is Optional — absence means "not
                         discoverable", never "invent something reasonable".
extractors/
  openapi_extractor.py   Loads the OpenAPI JSON (file or URL), walks paths/
                         operations into Endpoint objects: method, path,
                         summary/description, tag, path/query/header params,
                         auth requirement + scheme names. Also reads `info.version`.
  dto_extractor.py       Resolves $ref schemas into field lists, recursively:
                         a field whose own type (or array-item type) is itself
                         an object schema with properties gets its own fields
                         attached to FieldSpec.nested (e.g. a search request's
                         `filters: array<ContractFilter>` expands into
                         ContractFilter's field/operator/value). A visited-
                         schema-name guard turns self-referential schemas
                         (e.g. a tree node's own `children`) into a
                         `FieldSpec.recursive_ref` marker instead of expanding
                         forever. Also understands three recurring response
                         shapes: the ApiResponse<T> envelope, Spring's Page<T>
                         serialization, and plain arrays.
  validation_extractor.py  Turns one resolved schema property into a
                         FieldSpec (required / minLength / maxLength /
                         pattern / format / enum) — these are Bean
                         Validation constraints springdoc already translated
                         into OpenAPI keywords; nothing here re-reads Java.
  security_extractor.py  BEST-EFFORT, only runs when a module source root is
                         available. Locates a controller method's real Java
                         source (by re-deriving its route from
                         @RequestMapping/@XxxMapping and matching verb+path —
                         not by guessing off the method name, since names like
                         "create"/"search" repeat across every controller),
                         then looks for @PreAuthorize/@Secured on that method
                         or on the service method it delegates to. Extracts
                         only the SecurityPermissions.XXX identifiers, never
                         evaluates the SpEL expression.
  exception_extractor.py  BEST-EFFORT, only runs when a module source root is
                         available. Finds every `*ErrorCodes.java` file under
                         the module's own source tree and lists its constants
                         as a module-level "Known Error Codes" appendix.
  error_mapping_extractor.py  BEST-EFFORT, only runs when shared/common
                         source roots are available. Reads the shared,
                         module-independent Status -> HttpStatus table from
                         OperationCodeImpl, the framework-level error codes
                         (validation, not-found, forbidden, ...) with their
                         real HTTP status from GlobalExceptionHandler, and
                         (per module) the Status a module's own error code
                         was thrown with, wherever a throw site literally
                         names both together. Never infers a mapping from a
                         code's name or value.
  common_headers_extractor.py  BEST-EFFORT, only runs when shared/common
                         source roots are available. Finds headers applied
                         globally by shared servlet filters (e.g. a
                         correlation-ID header) — structurally invisible in
                         OpenAPI, since filters run outside the request-mapping
                         layer springdoc introspects.
  response_model_extractor.py  Detects the shared ApiResponse<T> envelope
                         shape once (springdoc names each generic
                         instantiation differently, e.g.
                         "ApiResponseLegalEntityResponse", so this matches by
                         key-set rather than a fixed schema name), walking
                         every declared property generically (not a fixed
                         key subset) so any envelope field is documented, and
                         reusing dto_extractor's recursive expansion for
                         nested fields like error.fieldErrors.
renderers/
  base.py                Renderer interface: render(ApiDocument) -> {path: text}.
                         A renderer only renders — it must never call an
                         extractor or infer missing data itself.
  markdown_renderer.py   index.md (module overview + shared sections) +
                         endpoints/<group>/<slug>.md per endpoint. Flattens
                         FieldSpec.nested into dotted-path rows (parent.child,
                         parent[].child) recursively, so nested DTOs render as
                         real fields instead of an opaque type name.
sync.py                  Documentation synchronization layer. Runs strictly
                         AFTER rendering — compares the freshly rendered
                         {path: text} against what's already under the output
                         directory, classifies each endpoint as
                         added/removed/updated/unchanged, diffs index.md's
                         shared sections, and decides what to write/delete.
                         Never calls an extractor, never formats markdown.
```

## Data flow

```
compiled Spring Boot app
        │  springdoc-openapi
        ▼
   OpenAPI JSON  ──────────────►  openapi_extractor  ──►  Endpoint list
        │                              │
        │                              ├─► dto_extractor (+ validation_extractor, recursive)
        │                              │      per operation's request/response
        │                              │
        │                         (needs module source root, optional)
        │                              ├─► security_extractor       → permissions
        │                              └─► exception_extractor      → module error codes
        │
        │                         (needs shared/common source roots, optional)
        │                              ├─► error_mapping_extractor  → error codes' Status/HTTP
        │                              │      status + framework-level codes + shared table
        │                              └─► common_headers_extractor → globally-applied headers
        │
        └─────────────────────────►  response_model_extractor  → shared envelope
                                     dto_extractor.find_page_envelope → pagination envelope

                    ApiDocument (fully populated)
                              │
                              ▼
                     MarkdownRenderer.render()
                              │
                              ▼
                    {relative_path: file_content}
                              │
                              ▼
                       sync.py (see "Execution modes")
```

Extraction and rendering are fully separated: extractors never format text,
renderers never call `json.load` or open a Java file. To add a new output
format later (OpenAPI YAML, Postman/Insomnia collection, frontend model
stubs), write a new `Renderer` subclass — no extractor changes needed.

## Automatic discovery

`discovery.py` is what makes `--module ORG --function generate` sufficient on
its own. It reads real, versioned repository artifacts — not a per-module
lookup table — so a brand-new module works the moment it follows the same
conventions every current module already does, with zero generator changes:

1. **Which springdoc group is this module?** Scans the backend tree for
   `GroupedOpenApi.builder()...group("X").displayName("Y").packagesToScan("Z", ...)`
   bean declarations (wherever they live — today that's `erp-main`'s
   `OpenApiConfig.java`, but discovery doesn't assume a path) and matches
   `--module` against each bean's method name / group id / display name
   first, falling back to its scanned package(s) only if nothing more
   specific matched (and preferring the most specific match, so a combined
   "all modules" group never wins by accident).
2. **What's the OpenAPI URL?** `http://localhost:<port>/api-docs/<group-id>`,
   where `<port>` is read from the backend's own
   `@Value("${server.port:XXXX})` default.
3. **Where's the module's own source?** Whichever Maven module's
   `src/main/java` actually contains the matched group's controller
   package(s) — found by existence check, not by guessing a directory name
   like `erp-org`.
4. **Where's its shared/common source?** Every *other* reactor module (from
   the root `pom.xml`'s `<modules>` list) that this module's own `pom.xml`
   declares as a `<dependency>` — the real, build-enforced dependency graph,
   not an assumption that it's specifically called `erp-common-utils` (a
   module can depend on several; all of them are searched).
5. **Where does output go?** `governance-repo/modules/<MODULE>/api-docs/` —
   this tool's own repository, so no discovery is needed, just the
   already-established convention.

Every step degrades to "not found" rather than guessing, exactly like the
existing best-effort extractors already do — `generate.py` turns that into a
clear error naming the explicit override flag to use instead.

`discovery.resolve()` returns one `RepositoryContext` object (module,
resolved OpenAPI source, module source root, shared/common source roots,
output directory) — `generate.py` passes it straight to `generator.run()`
without unpacking it into separate arguments itself. Repository-shaped
knowledge lives in exactly one place this way: no extractor imports
`discovery.py`, walks up looking for a `pom.xml`, or otherwise knows the
repository is laid out as sibling checkouts at all — each one just receives
whatever specific `Path`(s) `generator.build_document()` hands it.

```bash
# Explicit overrides — only for what discovery genuinely can't resolve
# (server not running on its default port, an unusual checkout, a saved
# OpenAPI file instead of a live server, ...). Never required for normal use.
python3 generate.py --module ORG --function generate \
    --openapi ./openapi.json \
    --source ../../../backend/erp-org/src/main/java \
    --common-source ../../../backend/erp-common-utils/src/main/java \
    --output ../../modules/ORG/api-docs/
```

## Execution modes (`--function`)

The generator supports the full documentation lifecycle, not just first-time
generation, so it's suitable for continuous use during backend development.
`sync.py` is a dedicated layer that runs after rendering — it never touches
extractors or renderers, and they don't know it exists.

- **generate** — full write, as if no docs exist yet. Every generated file
  is stamped with a marker line
  (`<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->`).
- **update** — re-extracts from the current backend, renders in memory, then
  compares against what's already under the output directory. Writes only
  added/changed files, deletes endpoint files for endpoints that no longer
  exist in the backend, leaves unchanged files alone, and never touches a
  file that doesn't carry the marker (hand-written docs, or a generated file
  a human has since edited) — those are reported as conflicts instead of
  being clobbered.
- **review** — runs the same comparison as update but writes nothing. Use it
  to answer "what changed in the API docs?" (e.g. in a PR check) without
  modifying anything.

`--function` is required and explicit (no auto-detection ambiguity for CI/CD
— a pipeline always says exactly what it wants). If you're generating output
for the very first time against an existing, hand-authored `api-docs/`
folder from before this tool tracked its own output with the marker line,
that first run will report every file as a conflict (unmanaged) — run
`--function generate` once to (re)stamp it, or move the old folder aside.

Because rendering is a deterministic function of the current backend state,
the comparison never needs to diff DTOs/permissions/validations/error-codes
individually — any such change already shows up as a content difference in
the affected endpoint's file (or in index.md's shared sections), so it's
picked up automatically.

Example update-mode report:

```
Mode        : Update
Added       : 1
  + POST /widgets/{id}/archive
Removed     : 1
  - deleteLegacyWidget (no longer present in the backend)
Updated     : 2
  ~ GET /widgets
  ~ POST /widgets
Unchanged   : 0
Shared docs : API Catalog (changed)
Files written: 4, deleted: 1
```

Output layout (one `api-docs/` per module, alongside its other governance
packages, not a shared `output/` under the tool itself):

```
modules/ORG/api-docs/
├── index.md                          module overview, auth, common headers,
│                                      shared response/pagination envelopes,
│                                      known error codes (+ Status/HTTP status
│                                      where discoverable), API catalog
└── endpoints/
    └── <group-slug>/
        └── <operationId-or-method-path-slug>.md
```

## Supported Spring Boot features

- Any HTTP method/path springdoc exposes, grouped by `@Tag`.
- Path/query/header parameters (works whether pagination is a custom
  `SearchRequest` DTO body, a raw `Pageable` method parameter, or ad-hoc
  `@RequestParam`/`@RequestHeader`s — all fully visible in the OpenAPI JSON,
  no special-casing needed).
- Request/response DTO fields with `@NotBlank`/`@NotNull` → required,
  `@Size` → min/maxLength, `@Pattern` → pattern, `@Schema(description,
  example)` → description/example — expanded recursively into nested fields
  when a field's own type is itself an object schema (search filter/sort
  contracts, field-error items, ...), with self-referential schemas rendered
  as an explicit "recursive" marker instead of expanding forever.
- `enum: [...]` values, when the schema actually has them (see Limitations).
- The shared `ApiResponse<T>` envelope (every declared field, not a fixed
  subset) and Spring `Page<T>` wrapper, each documented once rather than
  repeated on every endpoint.
- Global Bearer-auth requirement per operation, plus `info.version`.
- (best-effort, needs module source) Per-endpoint required permission,
  whether declared on the controller method or delegated to a service
  method.
- (best-effort, needs module source) Module-level known error codes from
  `*ErrorCodes.java`.
- (best-effort, needs shared/common source) Each error code's business
  `Status` and real HTTP status; the framework-level error codes and their
  HTTP status; headers applied globally by shared filters.

## Limitations

- **Per-endpoint error responses are not documented.** No controller in this
  codebase declares `@ApiResponse`/`@ApiResponses`, so there's nothing to
  extract per endpoint — only the module-level error-code appendix exists,
  now with Status/HTTP status attached where a throw site makes it
  discoverable, but still not attributed to a specific endpoint.
- **No `enum` schema arrays exist yet in this codebase** (e.g.
  finance-gl's `accountType` is a plain `String` documented only in
  free-text `@Operation` descriptions). The generator will render an
  `enum: [...]` list the moment a schema actually has one, but don't expect
  it today.
- **A search contract's `operator` allowed values are not discoverable.**
  `ContractFilter.operator` is a plain `String`; the values actually accepted
  live inside `BaseSearchContractRequest.mapOperator()`'s method body, not on
  any annotation. This is a backend annotation gap, not something this
  generator should reverse-engineer from arbitrary method bodies — see the
  enhancement report for why.
- **Permission and error/status discovery are source-text heuristics, not a
  real Java parser.** They rely on conventions actually observed across this
  codebase's modules (one delegate call per controller method under the same
  method name; `throw new (BusinessException|LocalizedException)(Status.X,
  SomeErrorCodes.Y, ...)` at the point of use). They silently return nothing
  for anything they can't confidently resolve — never guess.
- **Requires `/api-docs/<group>` to actually work as a path segment**, not a
  query string — springdoc groups aren't filterable via `?group=x`.

## Extension points

- **New output format**: add a `Renderer` subclass in `renderers/` (e.g.
  `renderers/openapi_yaml.py`, `renderers/postman.py`). It receives the same
  fully-populated `ApiDocument` — no extractor changes needed.
- **New extractor**: add a module under `extractors/` and call it from
  `generator.build_document()`, writing onto the existing
  `ApiDocument`/`Endpoint` dataclasses (add new Optional fields to
  `models/api_doc_model.py` as needed — never repurpose an existing field for
  a different meaning). Give it narrow, explicit parameters (a `Path`, a
  `dict`, ...), never `RepositoryContext` itself — `build_document()` is the
  one place that unpacks the context; keeping extractors one level removed
  from it is what keeps them free of any repository-layout assumptions.
- **New discovered input** (e.g. a future shared cross-module OpenAPI
  source): add a field to `discovery.RepositoryContext` and resolve it in
  `discovery.resolve()`. No other file needs to change its own repository
  logic — `generator.build_document()` just reads the new field.
- **Smarter permission/error resolution**: if this codebase ever adopts a
  `MethodSecurityExpressionHandler` introspection endpoint, or a real Java
  parser (e.g. javalang), `security_extractor.py`/`error_mapping_extractor.py`
  are the files that would need to change.
- **New shared index.md section** (e.g. Sorting, Search conventions): add it
  to `markdown_renderer._index_markdown`. `sync.py`'s section diff works off
  whatever `## ` headings actually appear, so review/update reports pick up
  the new section automatically — no `sync.py` change needed.
- **A new shared/cross-module contract worth surfacing** (e.g. a generic
  lookup/reference-data endpoint used by every module): would need a second,
  explicit OpenAPI input, since — unlike shared *source* — there is no
  build-enforced dependency graph linking one module's API to another's;
  see the enhancement report's discussion of why that one case stays an
  explicit override rather than auto-discovered.
