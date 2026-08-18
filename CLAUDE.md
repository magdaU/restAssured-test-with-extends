# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```powershell
# Run all tests
mvn test

# Run a specific test class
mvn -Dtest=VideoGameTests test
mvn -Dtest=FootbalTests test
mvn -Dtest=GpathJSONTest test
mvn -Dtest=GpathXMLTests test

# Run a single test method
mvn -Dtest=VideoGameTests#getAllGames test

# Run Football tests with API token
$env:FOOTBALL_DATA_API_TOKEN="your-token"
mvn -Dtest=FootbalTests test

# Or pass the token as a Maven property
mvn -Dfootball.api.token="your-token" -Dtest=FootbalTests test
```

## Architecture

This is a pure test project — there is no production `main` source code, only `src/test/java/`. All classes are scoped `test` in Maven.

**Config/base classes** (`src/test/java/config/`): Each API has a `*Config` class that runs `@BeforeClass` to set `RestAssured.requestSpecification` and `RestAssured.responseSpecification` as static globals. Test classes extend the config class to inherit these specs — this is the inheritance pattern used throughout the project.

- `VideoGameConfig` — sets base URI `https://videogamedb.uk/api/v2/`, JSON content type, and a global 200 status expectation.
- `FootballConfig` — sets base URI `https://api.football-data.org/v4`. The `X-Auth-Token` header is currently commented out and must be provided at runtime.
- `VideoGameEndpoints` — constants for VideoGame API paths (`/videogame`, `/videogame/{videoGameId}`).

**Test classes**:
- `VideoGameTests extends VideoGameConfig` — CRUD operations, POJO serialization/deserialization, JSON schema validation, XSD validation, response time assertions.
- `FootbalTests extends FootballConfig` — GETs against football-data.org: areas, teams, competitions. Tests hit live external APIs.
- `MyFirstVideoGame extends VideoGameConfig` — minimal introductory examples.

**POJO**: `objects/VideoGame.java` is used for Jackson serialization (request body) and deserialization (`response.getBody().as(VideoGame.class)`).

**Schema files** (`src/main/java/resources/`): `VideoGameJsonSchema.json` and `VideoGameXSD.xsd` are loaded from the classpath during schema validation tests.

## APIs Under Test

- **VideoGame DB**: `https://videogamedb.uk/api/v2/` — read-only; write operations (POST/PUT/DELETE) are accepted but not persisted. Swagger: `https://videogamedb.uk/swagger-ui/index.html`
- **Football Data**: `https://api.football-data.org/v4/` — requires a free API token from football-data.org. Without a token, requests return 403.

## How I Use AI in This Project

This project is developed with AI assistance (Claude Code), used deliberately and transparently rather than hidden.

**What I generate with AI's help:**
- Documentation (README sections, this file)
- Refactoring and cleanup of existing code
- Test boilerplate (class/method scaffolding, setup, annotations)

**What I always verify manually before accepting a change:**
- The full diff, before every commit — nothing gets committed unreviewed
- Assertion logic in tests — that a test actually checks what it claims to
- Links and URLs — badges, report links, CI links (this repo previously had stale links after a GitHub repo rename, which required checking live URL status rather than trusting what was written)
- Security — no secrets or tokens ever committed

**Where AI falls short for this kind of work:**
- Hallucinating repo/link state — assuming a URL or file exists without checking it against the live repo
- Missing business context — producing a technically valid fix without knowing the actual priority or intent behind a request
- Live, third-party APIs (e.g. football-data.org) — flaky, rate-limited, or stateful behavior that AI can't reliably reason about without running the tests