# Domain Layer Guideline

> Read this before generating or reviewing any backend code that implements a Business Rule.
> This document is **Governance**, not a generation Skill — it defines WHERE Business Rule
> ownership belongs. It does not define WHICH Skill produces which artifact; that remains an
> implementation detail of the Backend Skills (see "Scope" below).

## Purpose

The Domain Layer is the owner of all Business Rules. Its purpose is not to introduce DDD or
Hexagonal Architecture — it exists solely to keep Business Rules out of Application Services
and in the correct place.

**The Domain is the decision maker. The Service is the orchestrator.**

## The Decision Test

Whenever a piece of code answers the question:

> "Is this operation allowed?"

that code belongs in the Domain — never in the Service, Repository, Controller, Mapper, or the
persistence Entity.

## What the Domain owns

- Business Rules
- Business Decisions
- Business Validations
- Business Calculations
- State Transitions
- Domain Behaviors

## What the Domain must never contain

- REST APIs / HTTP objects
- Spring annotations (`@Service`, `@Component`, `@Entity`, `@Repository`, `@Transactional`)
- Repository or database access
- DTO mapping
- External integrations (email, SMS, messaging, other modules' Spring beans)

## Relationship to the Entity and the Service

```
<Entity>            → Persistence Model   (JPA — unchanged responsibility)
<Entity>Domain      → Business Rules      (this guideline)
<Entity>Service     → Orchestrator        (fetch, delegate, persist)
```

The Domain object is **not** the JPA Entity. It is a separate class. The Entity represents
persistence only; the Domain object represents business behavior only; the Service orchestrates
execution by fetching data, consulting the Domain object for a decision, then persisting the
outcome through the Entity and Repository as before.

## Construction

Domain objects are created only through factory methods:

- `create(...)` — validates and constructs a new instance; runs all construction-time Business
  Validations before returning.
- `from(...)` — reconstructs a Domain view for evaluating a rule before a mutation.

A public constructor used directly from outside the class is a violation.

## Domain Service Policy

- **Default:** do not create a Domain Service. A Business Rule belonging to a single Entity
  stays on that Entity's Domain object.
- **Optional:** introduce a Domain Service only when a Business Rule spans multiple Entities,
  requires coordination across multiple Repositories, or cannot naturally be attributed to a
  single Domain object.
- **Prohibited:** one Domain Service per Entity. If every entity in a module ends up with its
  own Domain Service, the rule belongs on the Domain object instead — that is not a genuine
  cross-entity concern.

## Scope — what this document does and does not decide

This guideline states an architectural requirement:

> The generated backend must contain a dedicated Domain object whenever the Execution Plan's
> Business Rules require Business Decision ownership for a given Entity.

It does **not** prescribe which Backend Generation Skill produces the Domain object or which
Skill performs the delegation to it. That is an implementation detail of the Backend Generation
Pipeline, governed by the Backend Skills themselves (see `.github/skills/backend/`), and may
change without requiring a change to this document.

## Relationship to the Execution Plan

The Execution Plan remains the single source of truth for **what** must be built, including
which Business Rules exist (tagged by RULE-ID) and where the Plan says they are enforced. This
guideline does not change or override the Execution Plan — it resolves the placement of rules
the Plan already requires, so that "enforced at entity/service layer" has one consistent,
unambiguous meaning across every module instead of being decided ad hoc per module.

## Golden Rule

The Domain is the decision maker.
The Service is the orchestrator.
