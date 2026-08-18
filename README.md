
# REST Assured Fundamentals

A Java-based API test project using the **REST Assured** library.  
Originally based on the [Rest Assured Fundamentals](https://www.udemy.com/course/rest-assured-fundamentals/?referralCode=2A76479D71A62609414D) course on Udemy and extended as part of a personal portfolio.

> 📌 **This project goes beyond the original tutorial.** The following improvements have been added independently:
> - **Allure Reporting** — test results published automatically via GitHub Pages with historical trend
> - **GitHub Actions CI** — automated test execution on every push and pull request
> - **Refactored test structure** — improved assertions, contract tests, data quality checks
> - **New test classes** — `VideoGameNegativeTests`, `GpathVideoGameTests`, `VideoGameXmlTests`
> - **Dynamic token handling** — Football API token loaded from environment variable or JVM property
> - **POJO improvements** — added `toString()`, fixed field consistency
> - **Maven configuration** — dependency versions extracted to properties, added Surefire plugin

[![Allure Report](https://img.shields.io/badge/Allure_Report-view%20results-orange)](https://magdau.github.io/restAssured-test-with-extends/)
[![CI](https://github.com/magdaU/restAssured-test-with-extends/actions/workflows/allure-report.yml/badge.svg)](https://github.com/magdaU/restAssured-test-with-extends/actions/workflows/allure-report.yml)

**Live Allure Report:** https://magdau.github.io/restAssured-test-with-extends/

---
## 📋 Technology Stack

| Technology            | Version  |
|-----------------------|----------|
| Java                  | 18       |
| Maven                 | 3.x      |
| REST Assured          | 5.3.0    |
| JUnit                 | 4.13.2   |
| Jackson Databind      | 2.14.2   |
| JSON Schema Validator | 5.3.0    |
| Allure Report         | 2.27.0   |

---

## 🏗️ Project Structure

```
src/
├── test/
│   ├── java/
│   │   ├── config/
│   │   │   ├── FootballConfig.java                    # Base configuration for Football API
│   │   │   ├── VideoGameConfig.java                   # Base configuration for VideoGame API
│   │   │   ├── VideoGameEndpoints.java                # VideoGame API endpoint constants
│   │   │   ├── VideoGameTests.java                    # VideoGame API tests (CRUD, serialization, schemas)
│   │   │   ├── VideoGameNegativeTests.java            # Negative tests (404, invalid body, null fields)
│   │   │   ├── VideoGameParameterizedTests.java       # Parameterized tests for valid game IDs 1-5
│   │   │   └── VideoGameNegativeParameterizedTests.java # Parameterized tests for invalid game IDs
│   │   ├── objects/
│   │   │   └── VideoGame.java            # VideoGame POJO model
│   │   ├── FootbalTests.java             # Football API tests
│   │   ├── GpathJSONTest.java            # GPath tests on JSON responses
│   │   ├── GpathXMLTests.java            # GPath tests on XML responses
│   │   └── MyFirstVideoGame.java         # Basic examples
│   └── resources/
│       ├── VideoGameJsonSchema.json      # JSON Schema for response validation
│       └── VideoGameXSD.xsd              # XSD Schema for XML response validation
```

---

## 🎮 Video Game API

Tested API: **Video Game DB** – a simple, fictional video game database.

- Swagger UI: https://videogamedb.uk/swagger-ui/index.html
- Base URL: `https://videogamedb.uk/api/v2/`

> ⚠️ The API runs in **read-only mode** – create, update, and delete operations are not persisted.

### Endpoints

| Constant               | Path                         | Description                  |
|------------------------|------------------------------|------------------------------|
| `ALL_VIDEO_GAMES`      | `/videogame`                 | List all video games          |
| `SINGLE_VIDEO_GAME`    | `/videogame/{videoGameId}`   | Get details of a single game  |

### Tests (`VideoGameTests`)

- `getAllGames` – retrieve all games, assert 200
- `createNewGameByJSON` – create a game via JSON body
- `createNewGameByXML` – create a game via XML body
- `updateGame` – update a game (PUT)
- `deleteGame` – delete a game (DELETE)
- `getSingleGame` – retrieve a single game by ID (5)
- `testVideoGameSerializationJSON` – serialize a POJO to JSON and POST it
- `testVideoGameSerializationXML` – GET `/videogame/5` as XML and validate against XSD (`VideoGameXSD.xsd`)
- `testVideoGameSchemaJSON` – GET `/videogame/5` and validate the response against JSON Schema (`VideoGameJsonSchema.json`)
- `convertJsonToPojo` – GET `/videogame/5`, deserialize JSON response into a `VideoGame` POJO, and log its fields
- `catureResponseTime` – capture and print response time in milliseconds
- `assertOnResponseTime` – assert response time is under 1000 ms
- `getAllGamesVerifyListNotEmpty` – assert the returned game list is not empty
- `getSingleGameVerifyFields` – assert `id`, `name` and `category` fields on GET `/videogame/1`

### Tests (`VideoGameNegativeTests`)

- `getSingleGame_NotFound` – GET non-existent ID (99999) → assert 404
- `getSingleGame_NegativeId` – GET negative ID (-1) → assert 404
- `createGame_EmptyBody` – POST empty JSON body `{}` → assert 4xx
- `createGame_NullFields` – POST POJO with all-null fields → assert 4xx

### Tests (`VideoGameParameterizedTests`)

- `getSingleGame_ReturnsValidFields[gameId = 1..5]` – for each ID in 1-5: assert 200, non-null `name` and `category`, and `id` matches the requested value

### Tests (`VideoGameNegativeParameterizedTests`)

- `getSingleGame_InvalidId_Returns404[gameId = 0/-1/99999/MAX_VALUE]` – for each invalid ID: assert 404

---

## ⚽ Football API

Tested API: **football-data.org** – football competition data.

- Documentation: https://www.football-data.org/
- Base URL: `https://api.football-data.org/v4/`

### Authentication – API token required

Football tests require an API token from [football-data.org](https://www.football-data.org/).  
Without a token all Football tests return **HTTP 403 Forbidden**.

> ⚠️ The free plan allows **10 requests per minute**. Running the full test suite twice in quick succession returns **HTTP 429 Too Many Requests**. Wait ~1 minute between runs if you hit this limit.

Provide the token in one of two ways:

**Environment variable (PowerShell):**
```powershell
$env:FOOTBALL_DATA_API_TOKEN="your-token"
mvn -Dtest=FootbalTests test
```

**Maven/JVM property:**
```powershell
mvn -Dfootball.api.token="your-token" -Dtest=FootbalTests test
```

### Tests (`FootbalTests`)

| Test | Description | Typical result | Failure reason |
|---|---|---|---|
| `getDetailsOneAre` | GET `/areas?areas=2076` | ✅ Pass | HTTP 500 – transient server error |
| `getDetailsOfMultipleArea` | GET `/areas` with multiple IDs | ✅ Pass | HTTP 500 – transient server error |
| `getDataFounded` | Assert Arsenal founded year = 1886 | ✅ Pass | HTTP 500 – transient server error |
| `getFirstTeamName` | Assert Arsenal FC is in Premier League team list | ✅ Pass | HTTP 500 – transient server error |
| `getAllTeamData` | Print full team JSON | ✅ Pass | HTTP 500 – transient server error |
| `getAllTeamData_DoCheckFirst` | Extract response with status code check | ✅ Pass | HTTP 500 – transient server error |
| `extractHeaders` | Print response headers | ✅ Pass | HTTP 500 – transient server error |
| `extractHeadersTeamName` | Extract team name via `jsonPath()` | ✅ Pass | HTTP 500 – transient server error |
| `extractAllTeams` | List all Premier League teams | ✅ Pass | HTTP 500 – transient server error |
| `getCompetitions` | Assert competitions list is not empty | ✅ Pass | HTTP 500 – transient server error |
| `getTopScorersForPremierLeague` | Assert scorers list is not empty | ✅ Pass | HTTP 500 – transient server error |
| `getStandingsForPremierLeague` | Assert standings list is not empty | ✅ Pass | HTTP 500 – transient server error |

> ⚠️ All Football tests depend on an external live API. Failures with HTTP 500 indicate a transient server-side issue at football-data.org — rerunning after a short wait usually resolves them. Failures with HTTP 403 mean the token is missing. Failures with HTTP 429 mean the rate limit was exceeded (run too quickly in succession).

---

## 🔍 GPath Tests

### GpathJSONTest (JSON)
Advanced querying of JSON responses using **GPath** (Groovy Path) syntax against the **VideoGame API**:

- `extractMapOfElementsWithFind` – find a game by name using `find { it.name == 'Resident Evil 4' }`, returns as Map
- `extractSingleValueWithFind` – find a game by ID using `find { it.id == 1 }.name`
- `extractListOfValueWithFindAll` – list games with `reviewScore > 70` using `findAll { it.reviewScore > 70 }.name`
- `extractSingleValueWithHighestNumber` – game name with the highest ID using `max { it.id }.name`
- `extractMutlipleValuesAndSumThem` – sum of all game IDs using `collect { it.id }.sum()`

### GpathXMLTests (XML)
Querying XML responses using **GPath** and **XmlPath** against the **VideoGame API**.

> XML response structure: `<List><item category="..."><id/><name/><releaseDate/><reviewScore/><rating/></item></List>`

- `getFirstGameInList` – name of the first game: `List.item[0].name`
- `getAttribute` – read the `category` XML attribute: `List.item[0].@category`
- `getListOfXMlNodes` – all game names as a list: `List.item.name`
- `getListOfXMLNodesByFindAllOnAttribute` – filter games by category attribute: `List.item.findAll { it.@category == 'Shooter' }.name`
- `getSingleNode` – find a specific game by name and assert it equals the expected value: `List.item.find { it.name == 'Resident Evil 4' }.name`

---

## 🧪 Test Cases

Detailed business-level descriptions of each test with step-by-step scenarios.

---

### 🎮 VideoGameTests

---

#### `getAllGames`
| | |
|---|---|
| **Step 1** | Set base URI to `https://videogamedb.uk/api/v2/` |
| **Step 2** | Send a `GET` request to `/videogame` |
| **Step 3** | Verify response status code is `200 OK` |
| **Expected** | A JSON list of all available video games is returned |

---

#### `getSingleGame`
| | |
|---|---|
| **Step 1** | Set base URI to `https://videogamedb.uk/api/v2/` |
| **Step 2** | Send a `GET` request to `/videogame/5` |
| **Step 3** | Verify response status code is `200 OK` |
| **Expected** | A single video game object is returned |

---

#### `createNewGameByJSON`
| | |
|---|---|
| **Step 1** | Authenticate with `POST /authenticate` to obtain a JWT token |
| **Step 2** | Build a JSON request body with game name, category, rating, and release year |
| **Step 3** | Send a `POST` request to `/videogame` with `Content-Type: application/json` |
| **Step 4** | Verify response status code is `200 OK` |
| **Expected** | API confirms the new game has been received (not persisted in read-only mode) |

---

#### `createNewGameByXML`
| | |
|---|---|
| **Step 1** | Authenticate and obtain a JWT token |
| **Step 2** | Build an XML request body with game attributes |
| **Step 3** | Send a `POST` request to `/videogame` with `Content-Type: application/xml` |
| **Step 4** | Verify response status code is `200 OK` |
| **Expected** | API accepts the XML payload and confirms the operation |

---

#### `updateGame`
| | |
|---|---|
| **Step 1** | Authenticate and obtain a JWT token |
| **Step 2** | Build a JSON body with updated game data |
| **Step 3** | Send a `PUT` request to `/videogame/3` |
| **Step 4** | Verify response status code is `200 OK` |
| **Expected** | API confirms the update (not persisted in read-only mode) |

---

#### `deleteGame`
| | |
|---|---|
| **Step 1** | Authenticate and obtain a JWT token |
| **Step 2** | Send a `DELETE` request to `/videogame/3` |
| **Step 3** | Verify response status code is `200 OK` |
| **Expected** | API confirms the deletion (not persisted in read-only mode) |

---

#### `testVideoGameSchemaJSON`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame/5` |
| **Step 2** | Load `VideoGameJsonSchema.json` from classpath resources |
| **Step 3** | Validate the response body against the JSON Schema |
| **Expected** | Response structure matches the defined schema without validation errors |

---

#### `testVideoGameSerializationXML`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame/5` with `Accept: application/xml` |
| **Step 2** | Load `VideoGameXSD.xsd` from classpath resources |
| **Step 3** | Validate the XML response against the XSD schema |
| **Expected** | XML response is valid according to the XSD definition |

---

#### `convertJsonToPojo`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame/5` |
| **Step 2** | Deserialize the JSON response body into a `VideoGame` POJO |
| **Step 3** | Log all fields of the deserialized object (id, name, category, rating, reviewScore) |
| **Expected** | JSON successfully maps to a `VideoGame` Java object with all fields populated |

---

#### `assertOnResponseTime`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` |
| **Step 2** | Capture the total response time |
| **Step 3** | Assert that response time is less than `1000 ms` |
| **Expected** | API responds within the defined SLA threshold |

---

### ⚽ FootbalTests

---

#### `getDetailsOneAre`
| | |
|---|---|
| **Step 1** | Set base URI to `https://api.football-data.org/v4/` |
| **Step 2** | Add header `X-Auth-Token` with the API token |
| **Step 3** | Send a `GET` request to `/areas/2072` |
| **Step 4** | Verify response status code is `200 OK` |
| **Expected** | Area details object is returned with correct ID and name |

---

#### `getDetailsOfMultipleArea`
| | |
|---|---|
| **Step 1** | Authenticate with the API token header |
| **Step 2** | Send a `GET` request to `/areas` |
| **Step 3** | Verify response status code is `200 OK` |
| **Step 4** | Assert the response contains multiple area entries |
| **Expected** | A list of multiple geographic areas is returned |

---

#### `getDataFounded`
| | |
|---|---|
| **Step 1** | Authenticate with the API token header |
| **Step 2** | Send a `GET` request to `/teams/{teamId}` |
| **Step 3** | Verify response status code is `200 OK` |
| **Step 4** | Extract the `founded` field from the response |
| **Step 5** | Assert the founding year equals the expected value |
| **Expected** | Correct founding year is returned for the team |

---

#### `getFirstTeamName`
| | |
|---|---|
| **Step 1** | Authenticate with the API token header |
| **Step 2** | Send a `GET` request to `/competitions/PL/teams` |
| **Step 3** | Verify response status code is `200 OK` |
| **Step 4** | Extract the first team name from the `teams[0].name` path |
| **Expected** | The first team name from the Premier League is returned |

---

#### `getAllTeamData_DoCheckFirst`
| | |
|---|---|
| **Step 1** | Authenticate with the API token header |
| **Step 2** | Send a `GET` request to `/competitions/PL/teams` |
| **Step 3** | Extract the full `Response` object |
| **Step 4** | Assert response status code is `200 OK` |
| **Step 5** | Print all team names from the response |
| **Expected** | Status is validated before extracting and displaying team data |

---

#### `extractHeaders`
| | |
|---|---|
| **Step 1** | Authenticate with the API token header |
| **Step 2** | Send a `GET` request to `/competitions/PL/teams` |
| **Step 3** | Extract all response headers |
| **Step 4** | Print each header name and value |
| **Expected** | All HTTP response headers are accessible and printable |

---

#### `extractAllTeams`
| | |
|---|---|
| **Step 1** | Authenticate with the API token header |
| **Step 2** | Send a `GET` request to `/competitions/PL/teams` |
| **Step 3** | Verify response status code is `200 OK` |
| **Step 4** | Use `jsonPath()` to extract the full list of team names |
| **Step 5** | Assert the list is not empty and print all team names |
| **Expected** | All Premier League team names are returned in a list |

---

### 🔍 GpathJSONTest

---

#### `extractMapOfElementsWithFind`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` |
| **Step 2** | Apply GPath expression: `find { it.name == 'Resident Evil 4' }` |
| **Step 3** | Extract the matching game as a Map and assert it is not null |
| **Expected** | A single game map for Resident Evil 4 is returned |

---

#### `extractSingleValueWithFind`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` |
| **Step 2** | Apply GPath expression: `find { it.id == 1 }.name` |
| **Step 3** | Assert the returned game name is not null |
| **Expected** | The name of the game with ID 1 is returned |

---

#### `extractListOfValueWithFindAll`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` |
| **Step 2** | Apply GPath `findAll { it.reviewScore > 70 }.name` |
| **Step 3** | Assert the returned list is not empty |
| **Expected** | A filtered list of game names with reviewScore above 70 is returned |

---

#### `extractSingleValueWithHighestNumber`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` |
| **Step 2** | Apply GPath `max { it.id }.name` to find the game with the highest ID |
| **Step 3** | Assert the returned name is not null |
| **Expected** | The name of the game with the maximum ID is returned |

---

#### `extractMutlipleValuesAndSumThem`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` |
| **Step 2** | Apply GPath `collect { it.id }.sum()` to sum all game IDs |
| **Step 3** | Print the result |
| **Expected** | The total sum of all game IDs is returned as a number |

---

### 🔍 GpathXMLTests

---

#### `getFirstGameInList`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` with `Accept: application/xml` |
| **Step 2** | Parse the XML response with `XmlPath` |
| **Step 3** | Extract `List.item[0].name` |
| **Expected** | The name of the first game in the XML list is returned and is not null |

---

#### `getAttribute`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` with `Accept: application/xml` |
| **Step 2** | Parse the XML response with `XmlPath` |
| **Step 3** | Extract the `category` XML attribute: `List.item[0].@category` |
| **Expected** | The category attribute of the first game is returned and is not null |

---

#### `getListOfXMLNodesByFindAllOnAttribute`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` with `Accept: application/xml` |
| **Step 2** | Apply GPath `List.item.findAll { it.@category == 'Shooter' }.name` |
| **Step 3** | Assert the returned list is not empty |
| **Expected** | All game names belonging to the 'Shooter' category are returned |

---

#### `getSingleNode`
| | |
|---|---|
| **Step 1** | Send a `GET` request to `/videogame` with `Accept: application/xml` |
| **Step 2** | Apply GPath `List.item.find { it.name == 'Resident Evil 4' }.name` |
| **Step 3** | Assert the returned name equals `"Resident Evil 4"` |
| **Expected** | The game name 'Resident Evil 4' is found and matches exactly |

---

## 🚀 CI/CD & Allure Report on GitHub Pages

Every push to `main` automatically:
1. Runs `VideoGameTests`, `VideoGameNegativeTests`, `VideoGameParameterizedTests`, `VideoGameNegativeParameterizedTests`, `GpathJSONTest`, `GpathXMLTests`, `MyFirstVideoGame` via GitHub Actions (Football tests excluded — require API token)
2. Fetches Allure `history/` from the previous deployment (enables trend graphs)
3. Generates the Allure HTML report with historical trend
4. Publishes it to **GitHub Pages** via the `gh-pages` branch

The **Allure Report** badge at the top of this file links directly to the live report.

### First-time GitHub Pages setup

Go to **Settings → Pages** in this repository and set:

| Setting | Value |
|---------|-------|
| Source  | **Deploy from a branch** |
| Branch  | `gh-pages` / `/ (root)` |

Save. The workflow pushes the report to the `gh-pages` branch on every push to `main`.

> **Note:** The first run generates a report without trend data (no history yet). From the second run onwards, each report includes a full trend graph.

### Re-run manually

Go to **Actions → Allure Report → Run workflow** to trigger a fresh run without pushing code.

---

## ⚡ Performance Testing (k6)

A [k6](https://k6.io/) load test (`performance/videogame-load-test.js`) complements the functional REST Assured suite by checking how the **VideoGame DB API** behaves under concurrent load.

- **VideoGame DB only** — it's public, read-only, and unauthenticated, so it's safe to load-test. The **Football Data API is excluded**: it requires a token and is rate-limited to 10 requests/minute on the free tier, so a load test would just return HTTP 429 immediately and prove nothing.
- **Load profile**: ramps 0→10 virtual users over 30s, holds 10 VUs for 30s, ramps back down over 10s — light enough to be a respectful load against a shared third-party sandbox.
- **Thresholds**: `p(95) < 1000ms` response time and `<1%` error rate. The latency threshold deliberately matches the `assertOnResponseTime` SLA already asserted in `VideoGameTests`, so the same 1000ms performance bar is checked both functionally (single request) and under load.

### Run locally

Requires the [k6 CLI](https://k6.io/docs/get-started/installation/) installed separately (it's a standalone binary, not a Maven dependency).

```powershell
k6 run performance/videogame-load-test.js
```

### Run in CI

Go to **Actions → k6 Load Test → Run workflow**. It's triggered manually (`workflow_dispatch`) rather than on every push, to avoid hammering the shared sandbox API for changes unrelated to performance.

---

## ▶️ Running Tests

### All tests
```powershell
mvn test
```

### VideoGame tests only
```powershell
mvn -Dtest=VideoGameTests test
```

### GPath tests only
```powershell
mvn -Dtest=GpathJSONTest test
mvn -Dtest=GpathXMLTests test
```

### Allure report

```powershell
# Run tests (results saved to target/allure-results/)
mvn -Dtest=VideoGameTests test

# Generate static HTML report → target/site/allure-maven-plugin/index.html
mvn allure:report

# Open live report in browser (auto-starts a local server)
mvn allure:serve
```

---

### All tests except Football (no token required)
```powershell
mvn "-Dtest=VideoGameTests,GpathJSONTest,GpathXMLTests,MyFirstVideoGame" test
```

### Football tests only (API token required)
```powershell
$env:FOOTBALL_DATA_API_TOKEN="your-token"
mvn -Dtest=FootbalTests test
```

> **Note:** Football tests require a free API token from [football-data.org](https://www.football-data.org/). Without a token all Football tests return HTTP 403. The free plan allows 10 requests/min — HTTP 429 is returned if the limit is exceeded.

### Test suite summary

| Test class | Tests | Stable | Known failure causes |
|---|---|---|---|
| `VideoGameTests` | 14 | ✅ All pass | None – read-only sandbox API, always available |
| `VideoGameNegativeTests` | 4 | ✅ All pass | None – tests error responses from the sandbox API |
| `VideoGameParameterizedTests` | 5 | ✅ All pass | None – runs for game IDs 1-5 |
| `VideoGameNegativeParameterizedTests` | 4 | ✅ All pass | None – runs for invalid IDs 0, -1, 99999, MAX_VALUE |
| `FootbalTests` | 12 | ⚠️ Usually pass | HTTP 403 (missing token), HTTP 429 (rate limit), HTTP 500 (server error) |
| `GpathJSONTest` | 5 | ✅ All pass | None – read-only sandbox API, always available |
| `GpathXMLTests` | 5 | ✅ All pass | None – read-only sandbox API, always available |
| `MyFirstVideoGame` | 2 | ✅ All pass | None – read-only sandbox API, always available |
| **Total** | **51** | | |

---

## 📦 Maven Dependencies

```xml
<!-- REST Assured -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.3.0</version>
    <scope>test</scope>
</dependency>

<!-- JSON Schema Validator -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>5.3.0</version>
    <scope>test</scope>
</dependency>

<!-- Jackson Databind (POJO serialization/deserialization) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.14.2</version>
    <scope>test</scope>
</dependency>

<!-- JUnit -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>

<!-- Allure JUnit 4 adapter -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-junit4</artifactId>
    <version>2.27.0</version>
    <scope>test</scope>
</dependency>

<!-- Allure REST Assured filter -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-rest-assured</artifactId>
    <version>2.27.0</version>
    <scope>test</scope>
</dependency>
```

---

## 🔧 Useful Tools

| Tool                | Description                             | Link                                    |
|---------------------|-----------------------------------------|-----------------------------------------|
| jsonschema2pojo     | Generate POJOs from JSON / JSON Schema  | https://www.jsonschema2pojo.org/        |
| freeformatter       | Formatters, validators, minifiers       | https://freeformatter.com/              |
| jsonschemavalidator | Interactive JSON Schema validator       | https://www.jsonschemavalidator.net/    |

---

## ✅ To Do

| # | Improvement | Status |
|---|-------------|--------|
| 1 | Runtime API token injection for Football API | ✅ Done |
| 2 | Rate limit throttling for Football API (free tier 10 req/min) | ✅ Done |
| 3 | Position-independent assertion in `getFirstTeamName` | ✅ Done |
| 4 | Allure reporting integration (JUnit 4 + REST Assured filter) | ✅ Done |
| 5 | Automated Allure report published to GitHub Pages via CI | ✅ Done |
| 6 | CI runs on `feature/**` and `fix/**` branches (tests only, no deploy) | ✅ Done |
| 7 | Negative tests — 404, invalid body, null fields (`VideoGameNegativeTests`) | ✅ Done |
| 8 | `@Step` annotations in Allure for multi-step tests (`VideoGameTests`) | ✅ Done |
| 9 | Parameterized tests — valid IDs 1-5 and invalid IDs (`@RunWith(Parameterized.class)`) | ✅ Done |
| 10 | Allure historical trend — `history/` preserved between CI runs via `gh-pages` branch | ✅ Done |
| 11 | k6 performance/load testing for VideoGame DB API | ✅ Done |

---

## 🛠️ Improvements

### 1. Runtime API token injection (`FootballConfig`)

**Problem:** The `X-Auth-Token` header was hardcoded and commented out in `FootballConfig`, meaning the token was never sent to the API. All Football tests returned HTTP 403 Forbidden regardless of what was passed on the command line.

**Fix:** `FootballConfig.setup()` now reads the token at runtime from two sources (in priority order):
1. JVM system property: `-Dfootball.api.token=<token>`
2. Environment variable: `FOOTBALL_DATA_API_TOKEN`

This keeps the token out of source control and allows flexible CI/local usage.

---

### 2. Rate limit throttling (`FootballConfig`)

**Problem:** The football-data.org free plan allows **10 requests per minute**. With 12 test methods running back-to-back, the last 2 tests consistently received HTTP 429 Too Many Requests.

**Fix:** Added a `@Before` method (`rateLimitDelay`) in `FootballConfig` that sleeps 6 seconds before each test. At 10 requests/minute the safe inter-request interval is 6 s — this keeps the suite within the free-tier limit without requiring any changes to individual test classes.

---

### 3. Position-independent assertion in `getFirstTeamName` (`FootbalTests`)

**Problem:** The test asserted `teams.name[1] == "Arsenal FC"`, relying on Arsenal being at a specific index in the API response. The football-data.org API does not guarantee a stable team order, so the assertion broke when the order changed.

**Fix:** Replaced the index-based check with `hasItem("Arsenal FC")`, which verifies that Arsenal FC is present anywhere in the list. The test now passes regardless of the ordering returned by the API.

---

### 4. Allure reporting integration

**Problem:** Test results were only visible in Maven console output — no structured report, no request/response details, no historical trend.

**Fix:** Added `allure-junit4` and `allure-rest-assured` dependencies (v2.27.0). The `AllureRestAssured` filter is registered in both `VideoGameConfig` and `FootballConfig`, which automatically attaches the full HTTP request and response to every test in the report. All test methods are annotated with `@Feature`, `@Story`, and `@Description` to produce a structured, readable report.

Reports can be generated locally:
```powershell
mvn -Dtest=VideoGameTests test   # run tests
mvn allure:report                # generate HTML → target/site/allure-maven-plugin/
mvn allure:serve                 # open in browser
```

---

### 5. Automated Allure report published to GitHub Pages

**Problem:** The Allure report existed only on the developer's machine and was not accessible to others without running the tests locally.

**Fix:** Added a GitHub Actions workflow (`.github/workflows/allure-report.yml`) that triggers on every push to `main`. It runs all non-Football tests, generates the Allure report, and deploys it to GitHub Pages via the `gh-pages` branch using `peaceiris/actions-gh-pages`.

The live report is always available at:  
👉 **https://magdau.github.io/restAssured-test-with-extends/**

---

### 6. CI runs on feature and fix branches

**Problem:** The workflow only triggered on `main`, so tests on `feature/**` and `fix/**` branches were never validated automatically before merge.

**Fix:** Extended the workflow triggers to include `feature/**` and `fix/**` branches. Tests run on all matching branches, but the deploy-to-Pages step is conditionally skipped (`if: github.ref == 'refs/heads/main'`) so only `main` publishes the live report.

---

### 7. Negative tests (`VideoGameNegativeTests`)

**Problem:** All existing tests cover only the happy path — valid IDs, well-formed bodies, expected 200 responses. Error scenarios were untested.

**Fix:** Added `VideoGameNegativeTests` class with four tests covering:
- `getSingleGame_NotFound` — GET non-existent ID → 404
- `getSingleGame_NegativeId` — GET negative ID → 404
- `createGame_EmptyBody` — POST empty JSON body → 4xx
- `createGame_NullFields` — POST POJO with all-null fields → 4xx

The class extends `VideoGameConfig` and uses `@Before`/`@After` to temporarily disable the global `responseSpecification` (which expects 200) so that 4xx assertions work correctly.

---

### 8. `@Step` annotations for readable Allure reports

**Problem:** The Allure report showed request/response attachments but no logical breakdown of what each test was doing — all test logic was invisible in the report.

**Fix:** Refactored complex tests in `VideoGameTests` and all tests in `VideoGameNegativeTests` to use private `@Step`-annotated methods. Each logical operation (fetch, deserialize, validate schema, assert field, assert time) is now a named step visible in the Allure timeline. Example steps in the report:

```
✔ GET /videogame/1
✔ Assert field 'id' equals 1
✔ Assert field 'name' is not null
✔ Assert field 'category' is not null
```

---

### 9. Parameterized tests (`@RunWith(Parameterized.class)`)

**Problem:** Tests like `getSingleGame` were hardcoded to a single game ID. Testing multiple IDs required duplicating the test method, which is verbose and hard to maintain.

**Fix:** Added two parameterized test classes using the standard JUnit 4 `@RunWith(Parameterized.class)` runner:

- **`VideoGameParameterizedTests`** — runs `getSingleGame_ReturnsValidFields` for game IDs 1-5. Each run verifies the `id`, `name`, and `category` fields.
- **`VideoGameNegativeParameterizedTests`** — runs `getSingleGame_InvalidId_Returns404` for IDs `0`, `-1`, `99999`, and `Integer.MAX_VALUE`. Each run asserts a 404 response.

The `@Parameters(name = "gameId = {0}")` annotation gives each run a descriptive name in the Allure report and Surefire output:
```
getSingleGame_ReturnsValidFields[gameId = 1]
getSingleGame_ReturnsValidFields[gameId = 2]
...
getSingleGame_InvalidId_Returns404[gameId = 99999]
```

---

### 10. Allure historical trend

**Problem:** Each CI run generated a fresh Allure report without any reference to previous runs. The trend graph in Allure (pass/fail/flaky counts over time) was always empty because the `history/` folder was not preserved between builds.

**Fix:** The workflow now checks out the `gh-pages` branch at the start of each run and copies the `history/` folder from the previous report into `target/allure-results/history/` before generating the new report. Allure reads this folder during report generation and produces a populated trend graph. After publishing, the new `history/` is pushed to `gh-pages` as part of the report, ready for the next run.

The deployment was switched from `actions/upload-pages-artifact` + `actions/deploy-pages` (GitHub Actions source) to `peaceiris/actions-gh-pages` (branch source), which makes the full report — including `history/` — accessible on the `gh-pages` branch between runs.
