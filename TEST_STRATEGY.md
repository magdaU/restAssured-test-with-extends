# Test Strategy

How and why this project tests what it tests. A companion to the [README](README.md) —
the README says *what's here*; this document explains *the reasoning behind it*.

---

## 1. Objectives

Demonstrate a realistic, layered API test automation approach against two live third-party
REST APIs, covering:

- Functional correctness (happy path and negative/error scenarios)
- Data contract validation (JSON Schema, XSD)
- Data-driven coverage via parameterization
- Non-functional behavior under concurrent load
- Structured, historical, CI-integrated reporting

The project intentionally does **not** aim to exhaustively test either third-party API —
both are external services this project doesn't own. The goal is depth and rigor of
*technique* against a realistic surface area, not 100% endpoint coverage.

---

## 2. Systems Under Test

| System | Role in this strategy | Constraints that shape testing |
|---|---|---|
| **VideoGame DB** (`videogamedb.uk`) | Primary target — functional, contract, data-driven, and load testing | Public, unauthenticated, read-only sandbox (writes accepted but not persisted) — safe to hit hard and often |
| **Football Data** (`football-data.org`) | Secondary target — functional testing only | Requires a personal API token; free tier rate-limited to 10 req/min — shapes which testing is viable (see §6) |

VideoGame DB being a disposable, always-available sandbox is *why* it carries the load
testing and the bulk of test volume — there's no risk of persisting bad data or exhausting
a quota. Football Data's constraints (auth + rate limit) are treated as first-class test
design inputs, not obstacles to work around silently — see `FootballConfig`'s token
injection and rate-limit throttling.

---

## 3. Test Levels & Techniques

### 3.1 Functional / happy-path (`VideoGameTests`, `FootbalTests`)
Standard CRUD + read verification against both APIs: status codes, response body field
assertions, response time SLA (`assertOnResponseTime` — under 1000ms). Establishes the
baseline: does the API do what it says under normal conditions.

### 3.2 Negative testing (`VideoGameNegativeTests`)
Equivalence-partitioning-driven: not-found ID, negative ID, empty request body, all-null
fields. Each targets a distinct failure class (missing resource vs. malformed input) rather
than varying the same invalid input multiple ways. Requires temporarily overriding the
suite's global 200-expecting response spec — a deliberate, scoped exception via `@Before`/
`@After`, not a structural change to the shared config.

### 3.3 Data-driven / parameterized (`VideoGameParameterizedTests`, `VideoGameNegativeParameterizedTests`)
JUnit 4 `@RunWith(Parameterized.class)` covering:
- Valid-path boundary: game IDs 1–5 (the full known-good range)
- Invalid-path boundary: `0`, `-1`, `99999`, `Integer.MAX_VALUE` — zero, negative,
  out-of-range, and integer-overflow edge, i.e. classic boundary value analysis rather than
  arbitrary invalid inputs.

### 3.4 Contract / schema validation (`VideoGameTests`)
JSON Schema (`VideoGameJsonSchema.json`) and XSD (`VideoGameXSD.xsd`) validation on the
same endpoint in both formats. This is the project's contract-testing layer: it catches
structural drift (a field renamed, a type changed, a field silently dropped) that
field-by-field assertions alone would miss.

### 3.5 Query-layer testing (`GpathJSONTest`, `GpathXMLTests`)
GPath/XmlPath expressions (`find`, `findAll`, `max`, `collect`) exercised against both JSON
and XML representations of the *same* underlying data — deliberately mirrored so the same
five query patterns are validated to behave equivalently regardless of response format.

### 3.6 Non-functional / load testing (k6)
A dedicated tool (k6), not REST Assured, because load testing and functional testing are
different disciplines with different tooling needs — REST Assured executes one request per
assertion context; k6 is built to drive and measure many concurrent virtual users. Scoped to
VideoGame DB only (see §2), with a load profile (§6) sized to be respectful of shared,
externally-owned infrastructure rather than to find its breaking point. The `p(95)<1000ms`
threshold deliberately reuses the same SLA number as `assertOnResponseTime`, so the same
performance bar is asserted at both the single-request and concurrent-load level.

---

## 4. Tooling Rationale

| Tool | Why this, here |
|---|---|
| **REST Assured** | Fluent `given/when/then` DSL, first-class JSON Schema/XSD validation, native JsonPath/XmlPath/GPath querying, Hamcrest matcher integration — purpose-built for exactly this kind of API test suite. |
| **JUnit 4** | `@RunWith(Parameterized.class)` is the mechanism behind §3.3; matches the REST Assured ecosystem's most common pairing. |
| **Jackson** | POJO (de)serialization (`VideoGame.java`) — tests assert against typed Java fields, not just raw JSON paths, for the serialization-focused test cases. |
| **Allure** | Structured, historical reporting with request/response attachments, `@Step` breakdowns, and custom Environment/Categories widgets — turns console-only pass/fail into a reviewable artifact (see §7). |
| **k6** | Purpose-built load-testing tool, standalone from the Java/Maven toolchain — see §3.6. |
| **GitHub Actions** | CI/CD — see §6. |

---

## 5. Test Environment & Data Strategy

No test-environment provisioning, no database seeding, no teardown — both APIs are
already-hosted, externally-owned sandboxes:

- **VideoGame DB** is explicitly read-only (writes accepted, never persisted), so create/
  update/delete tests can run repeatedly with zero cleanup and zero risk of data drift
  between runs.
- **Football Data** is a live production API with real data — tests read only, never write,
  and treat the data itself as a fixed external fact (e.g. Arsenal's founding year) rather
  than something under this project's control.

This is a deliberate trade-off: it makes the suite trivially runnable by anyone
(`mvn test` after cloning, no setup) at the cost of not controlling the data or the two
systems' availability. See §8 for how that trade-off is managed.

---

## 6. Risk-Based Scoping Decisions

Several explicit choices in this project are risk/cost trade-offs, not oversights:

- **Football tests excluded from CI** — no token secret is provisioned, and the free-tier
  rate limit (10 req/min) makes it a poor fit for a pipeline that might run frequently.
  Risk accepted: Football API regressions are only caught by a human running the suite
  locally with a token, not automatically on every push.
- **k6 load test is manual-only (`workflow_dispatch`), not triggered on push** — repeatedly
  driving concurrent load at a third-party sandbox for unrelated code changes (e.g. a
  README edit) would be disrespectful of infrastructure this project doesn't own, for no
  benefit. Trade-off: performance regressions aren't caught automatically per-commit, only
  when someone deliberately runs it.
- **k6 load profile is light** (ramps to 10 VUs, ~70s total, §3.6) — proving the API holds
  up under realistic light concurrent use, not stress-testing to find its breaking point,
  for the same reason.
- **Rate-limit throttling** (`FootballConfig`, 6s delay before each Football test) is a
  deliberate constraint satisfied at the config level rather than by reducing test count —
  coverage isn't sacrificed to work around the rate limit.

---

## 7. Reporting Strategy

Allure was chosen specifically to close the gap between "tests pass in a CI log" and
"someone can actually review what was tested and why a failure happened":

- **`@Feature`/`@Story`/`@Description`** annotations structure the report by business
  intent, not just class/method name.
- **`@Step`** breaks multi-action tests into named, reviewable steps in the timeline
  (see `VideoGameTests`/`VideoGameNegativeTests`).
- **`AllureRestAssured` filter** auto-attaches full request/response payloads to every test
  — no manual logging code per test.
- **Environment widget** (`allure-config/environment.properties`) documents what was
  actually tested (base URIs, framework/tool versions) alongside the results.
- **Categories widget** (`allure-config/categories.json`) separates the known,
  expected Football-403-without-token failures from real product/test defects — so a
  reviewer sees "9 expected auth failures" and "0 real defects" instead of "9 failures,"
  which would otherwise look identical to an actual regression.
- **Historical trend** — `history/` is preserved across CI runs via the `gh-pages` branch,
  so pass/fail/flaky rates are visible over time, not just for the latest run.

---

## 8. Known Limitations / Out of Scope

Stated explicitly rather than left implicit:

- **No security testing** (no auth bypass, injection, or fuzzing coverage) — out of scope
  for this project's goals.
- **No UI/E2E testing** — API layer only, by design.
- **No mocking/service virtualization** — tests hit live external APIs directly, which
  means occasional flakiness is *expected* (see `FootbalTests`' documented transient
  HTTP 500s) rather than something the suite tries to eliminate. This is a conscious
  trade-off: realistic end-to-end confidence over deterministic isolation.
- **Single environment** — no staging/prod split; both APIs are third-party, so there's
  only "the sandbox" to test against.
- **Football API coverage is shallow relative to VideoGame DB** — a direct consequence of
  the auth + rate-limit constraints in §2 and the scoping decisions in §6, not an
  oversight.
- **No contract testing against a schema registry / consumer-driven contracts** — the
  JSON Schema/XSD validation in §3.4 checks structure against a static, locally-stored
  schema, not a live, versioned contract source.

---

## 9. Where to Go Deeper

This document explains *why*. For exhaustive *what* — every test method, every
step-by-step scenario, the full CI workflow walkthrough, and k6 internals — see the
project's private developer notes (not published; ask the maintainer for access if you're
collaborating on this repo).
